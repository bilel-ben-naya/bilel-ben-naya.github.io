/* Port JavaScript de codes_parking_2.ino (Arduino Mega) et du backend FastAPI du dépôt. */
(function (root) {
  "use strict";
  var SEUIL_BARRIERE_CM = 6.0, OUVERTURE_MIN_MS = 2000, SEUIL_PLACE_CM = 5;
  var PERIODE_BARRIERE_MS = 100, PERIODE_PLACES_MS = 200, PERIODE_SERIAL_MS = 500;

  function Firmware() {
    this.dist = [-1, -1, -1];          // distances des 3 places (cm), -1 = pas d'écho
    this.dAvant = -1; this.dApres = -1; // capteurs de la barrière (cm)
    this.barriereOuverte = false; this.ouvertureManuelle = false; this.servo = 180;
    this.dernierInstantDetection = 0; this.placesLibres = 0;
    this.leds = [[0, 1], [0, 1], [0, 1]]; // [rouge, verte]
    this.lastOcc = [false, false, false]; this.tB = 0; this.tP = 0; this.tS = 0; this.serial = []; this.onChange = null;
  }
  Firmware.prototype.occ = function (i) { return this.dist[i] > 0 && this.dist[i] < SEUIL_PLACE_CM; };
  Firmware.prototype.updateBarriere = function (now) {
    if (this.ouvertureManuelle) { if (!this.barriereOuverte) { this.barriereOuverte = true; this.servo = 90; } return; }
    var detect = (this.dAvant > 0 && this.dAvant < SEUIL_BARRIERE_CM) || (this.dApres > 0 && this.dApres < SEUIL_BARRIERE_CM);
    if (detect) { this.dernierInstantDetection = now; if (!this.barriereOuverte) { this.barriereOuverte = true; this.servo = 90; } }
    if (this.barriereOuverte && now - this.dernierInstantDetection >= OUVERTURE_MIN_MS) { this.barriereOuverte = false; this.servo = 180; }
  };
  Firmware.prototype.updatePlaces = function () {
    var self = this, libres = 0, prev = this.lastOcc.slice();
    this.dist.forEach(function (d, i) { var o = self.occ(i); self.leds[i] = o ? [1, 0] : [0, 1]; if (!o) libres++; });
    this.placesLibres = libres; this.lastOcc = [0, 1, 2].map(function (i) { return self.occ(i); }); return prev;
  };
  Firmware.prototype.setManuel = function (on) {  // BLYNK_WRITE(V8)
    this.ouvertureManuelle = on;
    this.barriereOuverte = on; this.servo = on ? 90 : 180;
  };
  Firmware.prototype.status = function () {
    return "IP=172.30.1.200 | D1=" + this.dist[0] + " D2=" + this.dist[1] + " D3=" + this.dist[2] +
      " | Libres=" + this.placesLibres + " | Barriere=" + (this.barriereOuverte ? "OUVERTE" : "FERMEE") + " | Ethernet=OK | BlynkConnect=OK | BlynkRun=OK";
  };
  Firmware.prototype.blynk = function () {
    var o = this, f = function (i) { return o.occ(i) ? "Occupee" : "Libre"; };
    return { V0: this.placesLibres, V1: this.barriereOuverte ? "OUVERTE" : "FERMEE", V2: this.dist[0], V3: this.dist[1], V4: this.dist[2], V5: f(0), V6: f(1), V7: f(2) };
  };
  /* Équivalent de loop() : à appeler souvent avec millis(). Renvoie les changements d'état des places. */
  Firmware.prototype.loop = function (now) {
    var changes = [];
    if (now - this.tB >= PERIODE_BARRIERE_MS) { this.tB = now; this.updateBarriere(now); }
    if (now - this.tP >= PERIODE_PLACES_MS) {
      this.tP = now; var prev = this.updatePlaces(), self = this;
      prev.forEach(function (p, i) { var c = self.lastOcc[i]; if (p !== c) changes.push({ slot: i + 1, occupied: c }); });
    }
    if (now - this.tS >= PERIODE_SERIAL_MS) { this.tS = now; this.serial.push(this.status()); if (this.serial.length > 40) this.serial.shift(); }
    return changes;
  };

  /* Backend (backend/main.py), temps en minutes virtuelles. */
  function Backend() {
    this.total = 3; this.ppm = 0.05; this.slots = []; this.sessions = []; this.events = []; this.nextId = 1; this.nextEv = 1;
    for (var i = 1; i <= this.total; i++) this.slots.push({ id: i, occupied: false });
  }
  Backend.prototype.log = function (type, payload, t) { this.events.unshift({ id: this.nextEv++, type: type, payload: payload, t: t }); if (this.events.length > 30) this.events.pop(); };
  Backend.prototype.status = function () {
    var occ = this.slots.filter(function (s) { return s.occupied; }).length;
    return { total_spots: this.total, occupied: occ, available: Math.max(this.total - occ, 0), price_per_minute: this.ppm, slots: this.slots.map(function (s) { return { id: s.id, occupied: s.occupied }; }) };
  };
  Backend.prototype.updateSlot = function (slotId, occupied, plate, t) {
    var s = this.slots[slotId - 1]; if (!s) return { error: 404 };
    var old = s.occupied;
    if (old === occupied) { this.log("slot_no_change", "slot=" + slotId + ", occupied=" + occupied, t); return { ok: true, changed: false }; }
    s.occupied = occupied;
    this.log("slot_update", "slot=" + slotId + ", old=" + old + ", new=" + occupied + ", plate=" + (plate || "UNKNOWN"), t);
    var active = this.sessions.filter(function (x) { return x.slot_id === slotId && x.end_time === null; })[0];
    if (occupied) { if (!active) this.sessions.push({ id: this.nextId++, plate: plate || "UNKNOWN", slot_id: slotId, start_time: t, end_time: null, paid_amount: null }); }
    else if (active) active.end_time = t;
    return { ok: true, changed: true };
  };
  Backend.prototype.amount = function (s) { return Math.round(Math.max(s.end_time - s.start_time, 0) * this.ppm * 100) / 100; };
  Backend.prototype.pay = function (id) {
    var s = this.sessions.filter(function (x) { return x.id === id; })[0];
    if (!s) return { error: 404 }; if (s.end_time === null) return { error: 409, msg: "Stop d'abord la session" };
    if (s.paid_amount !== null) return { error: 409, msg: "Déjà payé" };
    s.paid_amount = this.amount(s); return { status: "paid", amount: s.paid_amount };
  };

  var api = { Firmware: Firmware, Backend: Backend };
  if (typeof module !== "undefined" && module.exports) module.exports = api; else root.Parking = api;
})(typeof window !== "undefined" ? window : globalThis);
