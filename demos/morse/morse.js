/* Port JavaScript du projet STM32 : émetteur (MC1) et récepteur (MC2).
   Les constantes, la table Morse et la logique de détection suivent les fichiers main.c d'origine. */
(function (root) {
  "use strict";
  var ALPHA = "abcdefghijklmnopqrstuvwxyz0123456789".split("");
  var CODE = ["01","1000","1010","100","0","0010","110","0000","00","0111","101","0100","11","10","111","0110","1101","010","000","1","001","0001","011","1001","1011","1100","11111","01111","00111","00011","00001","00000","10000","11000","11100","11110"];
  var UNIT = 200, THRESHOLD = 2000, DOT_MAX = 350, LETTER_GAP = 450, WORD_GAP = 1200;
  var LOW = 400, HIGH = 3000;

  /* Émetteur : liste d'intervalles sonores [début, fin] en ms, comme les HAL_Delay de MC1. */
  function emit(text, t0) {
    var t = t0 || 0, on = [], sent = "";
    text.split("").forEach(function (ch) {
      if (ch === "\r" || ch === "\n" || ch === " ") { t += 7 * UNIT; sent += " "; return; }
      var i = ALPHA.indexOf(ch.toLowerCase());
      if (i < 0) return;
      CODE[i].split("").forEach(function (s) {
        var d = s === "0" ? UNIT : 3 * UNIT;
        on.push([t, t + d]); t += d + UNIT;
      });
      t += 2 * UNIT; sent += ALPHA[i];
    });
    return { on: on, end: t, sent: sent };
  }

  function rng(seed) { var s = seed >>> 0 || 1; return function () { s ^= s << 13; s >>>= 0; s ^= s >>> 17; s ^= s << 5; s >>>= 0; return s / 4294967296; }; }
  function gauss(r) { var u = 1 - r(), v = r(); return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v); }

  /* Signal vu par l'ADC, un échantillon par milliseconde. */
  function channel(on, total, noise, seed) {
    var r = rng(seed), n = new Array(total), i, k, k0 = 0;
    var base = new Uint8Array(total);
    on.forEach(function (iv) { for (i = iv[0]; i < iv[1] && i < total; i++) base[i] = 1; });
    var sd = noise * 900, hole = 0;
    for (i = 0; i < total; i++) {
      var v = base[i] ? HIGH : LOW;
      if (base[i] && noise > 0) {
        if (hole > 0) { hole--; v = LOW; }
        else if (r() < noise * 0.00004) hole = 2 + Math.floor(r() * 12);
      }
      v += gauss(r) * sd;
      n[i] = Math.max(0, Math.min(4095, Math.round(v)));
    }
    return n;
  }

  /* Récepteur : boucle principale de MC2, appelée une fois par milliseconde. */
  function receive(adc) {
    var morse = "", texte = "", uart = [];
    var soundStart = 0, silenceStart = 0, isSound = 0, letterDecoded = 0, wordSpace = 0;
    for (var now = 0; now < adc.length; now++) {
      var v = adc[now];
      if (v > THRESHOLD && !isSound) { isSound = 1; soundStart = now; letterDecoded = 0; wordSpace = 0; }
      if (v <= THRESHOLD && isSound) {
        var d = now - soundStart; isSound = 0; silenceStart = now;
        if (morse.length < 99) morse += d < DOT_MAX ? "0" : "1";
      }
      if (!isSound && silenceStart !== 0) {
        var s = now - silenceStart;
        if (s > LETTER_GAP && !letterDecoded && morse.length > 0) {
          var ind = CODE.indexOf(morse);
          if (texte.length < 99) { texte += ind !== -1 ? ALPHA[ind] : "?"; }
          uart.push({ t: now, line: "Texte: " + texte });
          morse = ""; letterDecoded = 1;
        }
        if (s > WORD_GAP && !wordSpace) {
          if (texte.length < 99 && texte.length > 0 && texte[texte.length - 1] !== " ") { texte += " "; uart.push({ t: now, line: "Texte: " + texte }); }
          wordSpace = 1; silenceStart = 0;
        }
      }
    }
    return { texte: texte, uart: uart };
  }

  function run(text, noise, seed) {
    var e = emit(text, 1000), total = e.end + 2500;
    var adc = channel(e.on, total, noise || 0, seed || 1);
    var r = receive(adc);
    return { on: e.on, sent: e.sent.replace(/ +$/, ""), adc: adc, total: total, uart: r.uart, texte: r.texte.replace(/ +$/, "") };
  }

  var api = { ALPHA: ALPHA, CODE: CODE, UNIT: UNIT, THRESHOLD: THRESHOLD, emit: emit, channel: channel, receive: receive, run: run };
  if (typeof module !== "undefined" && module.exports) module.exports = api; else root.Morse = api;
})(typeof window !== "undefined" ? window : globalThis);
