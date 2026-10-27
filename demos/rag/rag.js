/* Port JavaScript de RecursiveCharacterTextSplitter (langchain) et recherche par mots (BM25). */
(function (root) {
  "use strict";
  function escRe(s) { return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&"); }
  function splitWithSep(text, sep) {
    var splits;
    if (sep) {
      var parts = text.split(new RegExp("(" + escRe(sep) + ")"));
      splits = [parts[0]];
      var rest = [];
      for (var i = 1; i < parts.length; i += 2) rest.push(parts[i] + (parts[i + 1] !== undefined ? parts[i + 1] : ""));
      splits = [parts[0]].concat(rest);
    } else splits = Array.from(text);
    return splits.filter(function (s) { return s !== ""; });
  }
  function joinDocs(docs, sep) { var t = docs.join(sep).trim(); return t === "" ? null : t; }
  function mergeSplits(splits, sep, size, overlap) {
    var sl = sep.length, docs = [], cur = [], total = 0;
    splits.forEach(function (d) {
      var l = d.length;
      if (total + l + (cur.length ? sl : 0) > size) {
        if (cur.length) {
          var doc = joinDocs(cur, sep); if (doc !== null) docs.push(doc);
          while (total > overlap || (total + l + (cur.length ? sl : 0) > size && total > 0)) {
            total -= cur[0].length + (cur.length > 1 ? sl : 0); cur = cur.slice(1);
          }
        }
      }
      cur.push(d); total += l + (cur.length > 1 ? sl : 0);
    });
    var last = joinDocs(cur, sep); if (last !== null) docs.push(last);
    return docs;
  }
  function splitText(text, seps, size, overlap) {
    var final = [], sep = seps[seps.length - 1], next = [];
    for (var i = 0; i < seps.length; i++) {
      var s = seps[i];
      if (s === "") { sep = s; break; }
      if (new RegExp(escRe(s)).test(text)) { sep = s; next = seps.slice(i + 1); break; }
    }
    var splits = splitWithSep(text, sep), good = [];
    splits.forEach(function (s) {
      if (s.length < size) good.push(s);
      else {
        if (good.length) { final = final.concat(mergeSplits(good, "", size, overlap)); good = []; }
        if (!next.length) final.push(s); else final = final.concat(splitText(s, next, size, overlap));
      }
    });
    if (good.length) final = final.concat(mergeSplits(good, "", size, overlap));
    return final;
  }
  function chunks(text, size, overlap) { return splitText(text, ["\n\n", "\n", " ", ""], size || 500, overlap === undefined ? 50 : overlap); }

  var STOP = "le la les un une des de du d l et en au aux à a est sont ce ces cette que qui quoi quel quelle quels quelles dans sur pour par avec sans ou où ne pas plus se sa son ses leur leurs il elle ils elles on nous vous je tu y the of and to in is".split(" ");
  function tok(s) {
    return (s.toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "").match(/[a-z0-9]{2,}/g) || []).filter(function (w) { return STOP.indexOf(w) < 0; });
  }
  function bm25(docs, query, k) {
    var N = docs.length, T = docs.map(tok), avg = T.reduce(function (a, t) { return a + t.length; }, 0) / (N || 1), df = {};
    T.forEach(function (t) { var seen = {}; t.forEach(function (w) { if (!seen[w]) { seen[w] = 1; df[w] = (df[w] || 0) + 1; } }); });
    var q = tok(query), k1 = 1.5, b = 0.75;
    var sc = T.map(function (t, i) {
      var tf = {}; t.forEach(function (w) { tf[w] = (tf[w] || 0) + 1; });
      var s = 0; q.forEach(function (w) { if (!tf[w]) return; var idf = Math.log(1 + (N - df[w] + 0.5) / (df[w] + 0.5)); s += idf * tf[w] * (k1 + 1) / (tf[w] + k1 * (1 - b + b * t.length / (avg || 1))); });
      return { i: i, s: s };
    });
    return sc.sort(function (a, b) { return b.s - a.s || a.i - b.i; }).slice(0, k || 2);
  }
  var api = { chunks: chunks, bm25: bm25, tok: tok };
  if (typeof module !== "undefined" && module.exports) module.exports = api; else root.RagLib = api;
})(typeof window !== "undefined" ? window : globalThis);
