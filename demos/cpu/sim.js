/* Port JavaScript fidèle du simulateur C (fifo.c, sjf.c, sjrf.c, rr.c, metrics.c, gantt.c). */
(function(root){
"use strict";
var PRET=0, EN_COURS=1, EN_ATTENTE=2, TERMINE=3;

function clone(list){
  return list.map(function(p){
    return {pid:p.pid, arr:p.arr, cpu:p.cpu, es:p.es, type:p.type, deb:p.deb,
      rest:p.cpu, finEs:-1, etat:PRET, debut:-1, fin:0, attente:0, reponse:0, restit:0};
  });
}
function gantt(g, pid, t){
  var l = g.length ? g[g.length-1] : null;
  if(l && l.pid === pid && l.fin === t){ l.fin = t + 1; return; }
  g.push({pid:pid, debut:t, fin:t+1});
}
function ioPhase(P, hasQueue, enqueue){
  var i, p, bloque = false;
  for(i=0;i<P.length;i++){ p=P[i];
    if(p.etat===EN_ATTENTE && p.type===1 && p.finEs!==-1 && p.finEs<=t_){ p.etat=PRET; if(enqueue) enqueue(i); }
  }
  for(i=0;i<P.length;i++){ p=P[i];
    if(p.etat===EN_ATTENTE && p.type===2 && p.finEs!==-1){
      if(p.finEs<=t_){ p.etat=PRET; if(enqueue) enqueue(i); } else bloque = true;
    }
  }
  return bloque;
}
var t_ = 0;
function finish(p, h){
  p.fin=h; p.restit=h-p.arr; p.attente=p.restit-p.cpu-p.es; p.etat=TERMINE;
}
function afterTick(p, h){
  /* retourne 'io' | 'done' | 'run' */
  var fait = p.cpu - p.rest;
  if(p.es>0 && p.finEs===-1 && fait===p.deb){ p.etat=EN_ATTENTE; p.finEs=h+p.es; return "io"; }
  if(p.rest===0){ finish(p,h); return "done"; }
  return "run";
}
function start(p,h){ if(p.debut===-1){ p.debut=h; p.reponse=h-p.arr; } }

function run(algo, list, quantum){
  var P = clone(list), g = [], n = P.length, h = 0, term = 0, i, r;
  if(algo==="FIFO") P.sort(function(a,b){ return a.arr-b.arr; }); /* tri stable comme le tri à bulles */
  var elu_actuel=-1, file=[], dans=[], qrest=0;
  for(i=0;i<n;i++) dans[i]=0;
  function enq(i){ if(!dans[i]){ file.push(i); dans[i]=1; } }
  var guard = 0;
  while(term<n){
    if(++guard>200000) throw new Error("boucle");
    t_ = h;
    var bloque = ioPhase(P, algo==="RR", algo==="RR" ? enq : null);
    if(bloque){
      if(algo==="SJRF"){ if(elu_actuel!==-1 && P[elu_actuel].etat===EN_COURS) P[elu_actuel].etat=PRET; elu_actuel=-1; }
      if(algo==="RR"){
        if(elu_actuel!==-1 && P[elu_actuel].etat===EN_COURS){
          P[elu_actuel].etat=PRET;
          if(!dans[elu_actuel]){ file.unshift(elu_actuel); dans[elu_actuel]=1; }
        }
        elu_actuel=-1; qrest=0;
      }
      h++; continue;
    }
    var elu=-1;
    if(algo==="FIFO"){
      for(i=0;i<n;i++) if(P[i].etat===PRET && P[i].arr<=h){ elu=i; break; }
    } else if(algo==="SJF"){
      for(i=0;i<n;i++) if(P[i].etat===PRET && P[i].arr<=h){
        if(elu===-1 || P[i].cpu<P[elu].cpu || (P[i].cpu===P[elu].cpu && P[i].arr<P[elu].arr)) elu=i;
      }
    } else if(algo==="SJRF"){
      for(i=0;i<n;i++) if((P[i].etat===PRET||P[i].etat===EN_COURS) && P[i].arr<=h){
        if(elu===-1 || P[i].rest<P[elu].rest) elu=i;
      }
    } else { /* RR */
      for(i=0;i<n;i++) if(P[i].etat===PRET && P[i].arr===h) enq(i);
      if(elu_actuel===-1 || qrest===0){
        if(elu_actuel!==-1 && P[elu_actuel].etat===EN_COURS){ P[elu_actuel].etat=PRET; enq(elu_actuel); }
        while(file.length && P[file[0]].etat===TERMINE) file.shift();
        if(!file.length){ h++; continue; }
        elu_actuel=file.shift(); dans[elu_actuel]=0; qrest=quantum;
      }
      elu=elu_actuel;
      if(P[elu].arr>h){ h++; continue; }
    }
    if(elu===-1){ h++; continue; }
    if(algo==="SJRF"){
      if(elu_actuel!==-1 && elu_actuel!==elu && P[elu_actuel].etat===EN_COURS) P[elu_actuel].etat=PRET;
      elu_actuel=elu;
    }
    var p=P[elu];
    start(p,h); p.etat=EN_COURS; p.rest--;
    if(algo==="RR") qrest--;
    gantt(g,p.pid,h); h++;
    r=afterTick(p,h);
    if(r==="io"){ if(algo==="SJRF"||algo==="RR"){ elu_actuel=-1; } if(algo==="RR") qrest=0; continue; }
    if(r==="done"){ if(algo==="SJRF"||algo==="RR") elu_actuel=-1; if(algo==="RR") qrest=0; term++; continue; }
    if(algo==="RR" && qrest===0 && p.etat===EN_COURS){ p.etat=PRET; enq(elu); elu_actuel=-1; }
    if(p.etat===EN_COURS) p.etat=PRET;
  }
  return metrics(P, g, algo);
}
function metrics(P, g, algo){
  var n=P.length, sa=0, sr=0, sp=0, fmax=0, amin=Infinity, tot=0;
  P.forEach(function(p){ sa+=p.attente; sr+=p.restit; sp+=p.reponse; if(p.fin>fmax) fmax=p.fin; if(p.arr<amin) amin=p.arr; tot+=p.cpu; });
  var dur=fmax-amin;
  return {algo:algo, procs:P.slice().sort(function(a,b){return a.pid-b.pid;}), order:P, gantt:g,
    moyAttente:sa/n, moyRestit:sr/n, moyReponse:sp/n, tauxCpu: dur>0?100*tot/dur:0, fin:fmax};
}
function parse(text){
  var out=[], err=null;
  text.split(/\r?\n/).forEach(function(l,idx){
    l=l.trim(); if(!l||l[0]==="#") return;
    var v=l.split(/\s+/).map(Number);
    if(v.length<6||v.some(function(x){return !isFinite(x)||x<0;})){ if(!err) err="Ligne "+(idx+1)+" : 6 entiers positifs attendus."; return; }
    out.push({pid:v[0],arr:v[1],cpu:v[2],es:v[3],type:v[4],deb:v[5]});
  });
  return {procs:out, error:err};
}
root.Sim = {run:run, parse:parse};
if(typeof module!=="undefined") module.exports = root.Sim;
})(typeof window!=="undefined"?window:globalThis);
