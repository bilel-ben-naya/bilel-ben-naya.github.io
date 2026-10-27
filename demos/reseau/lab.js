/* Actions du labo : ping, traceroute, nslookup, accès web direct ou via proxy. */
(function(root){
"use strict";
var N=root.Net, NODES=N.NODES;
var HOSTNAME={};Object.keys(NODES).forEach(function(k){HOSTNAME[k]=NODES[k].host;});
function nameOf(ip){var p=N.ptr(ip);return p?p+" ("+ip+")":ip;}
function nodeIp(k){return NODES[k].ifs[0].ip;}
function trip(S,from,pkt){
  var f=N.send(S,from,pkt,false);
  if(!f.ok)return {ok:false,fwd:f,why:f.reason,legs:[f.path]};
  var dest=f.arrived,svc=NODES[dest].svc;
  if(pkt.proto==="tcp"&&svc.tcp.indexOf(pkt.dport)<0)return {ok:false,fwd:f,refused:true,why:"connexion refusée par "+dest+" (aucun service sur le port "+pkt.dport+")",legs:[f.path]};
  if(pkt.proto==="udp"&&svc.udp.indexOf(pkt.dport)<0&&pkt.dport<33434)return {ok:false,fwd:f,why:"aucun service UDP "+pkt.dport+" sur "+dest,legs:[f.path]};
  var r=f.pkt,rev={proto:r.proto,src:r.dst,dst:r.src,sport:r.dport,dport:r.sport,setup:false,type:r.proto==="icmp"?0:undefined};
  var b=N.send(S,dest,rev,true);
  if(!b.ok)return {ok:false,fwd:f,back:b,why:"la réponse n'arrive pas : "+b.reason,legs:[f.path,b.path]};
  return {ok:true,fwd:f,back:b,dest:dest,legs:[f.path,b.path]};
}
function dns(S,from,name,L,legs){
  var q=name.toLowerCase().replace(/\.$/,"");
  var r=N.resolve(q);
  if(from==="EXT"){ if(!r){L.push("** serveur introuvable : "+name+" n'existe pas");return null;} return r.ip; }
  var servers=[["PC2","77.88.4.130"],["PC3","77.88.4.131"]],i;
  for(i=0;i<servers.length;i++){
    var sv=servers[i];
    if(sv[0]==="PC2"&&!S.opts.dns1){ if(from==="PC2"){continue;} var t0=trip(S,from,{proto:"udp",src:null,dst:sv[1],sport:40000,dport:53});L.push(";; pas de réponse de "+sv[1]+" (named arrêté sur PC2), essai du serveur secondaire");continue; }
    if(NODES[from].ifs.some(function(f){return f.ip===sv[1];})||from===sv[0]){ if(!r){L.push("** "+name+" : domaine inexistant (NXDOMAIN)");return null;} L.push("Serveur : "+sv[1]+" (local)");return r.ip; }
    var t=trip(S,from,{proto:"udp",src:null,dst:sv[1],sport:40000,dport:53});
    legs.push.apply(legs,t.legs);
    if(t.ok){ if(!r){L.push("Serveur : "+sv[1]+"\n** "+name+" : domaine inexistant (NXDOMAIN)");return null;} L.push("Serveur : "+sv[1]+(i?" (DNS secondaire)":" (DNS primaire)"));return r.ip; }
    L.push(";; "+sv[1]+" injoignable : "+t.why);
  }
  L.push("** aucun serveur DNS n'a répondu");return null;
}
function target(S,from,t,L,legs){
  t=t.trim();
  if(/^\d+\.\d+\.\d+\.\d+$/.test(t))return t;
  var ip=dns(S,from,t,L,legs);
  if(ip){var r=N.resolve(t);L.push("Nom : "+(r.cname?r.cname+" (alias de "+t+")":r.name)+"\nAdresse : "+ip);}
  return ip;
}
function run(S,from,action,tgt,opt){
  S.nat=[];var L=[],legs=[],ok=false;
  var t=tgt.trim().replace(/^https?:\/\//,"");var path="";var slash=t.indexOf("/");if(slash>-1){path=t.slice(slash);t=t.slice(0,slash);}
  if(!t){L.push("Indiquez une cible (nom ou adresse IP).");return {lines:L,legs:[],ok:false};}
  if(action==="nslookup"){
    if(/^\d+\.\d+\.\d+\.\d+$/.test(t)){var p=N.ptr(t);L.push(p?t+" : nom = "+p:"** "+t+" : pas d'enregistrement PTR");ok=!!p;}
    else{var ip=dns(S,from,t,L,legs);if(ip){var r=N.resolve(t);L.push("Nom : "+(r.cname?t+" est un alias de "+r.name:r.name)+"\nAdresse : "+ip);ok=true;}}
    return {lines:L,legs:legs,ok:ok};
  }
  var ip=target(S,from,t,L,legs);
  if(!ip)return {lines:L,legs:legs,ok:false};
  if(action==="ping"){
    var r=trip(S,from,{proto:"icmp",src:null,dst:ip,type:8});legs.push.apply(legs,r.legs);
    L.push("PING "+t+" ("+ip+")");
    if(r.ok){L.push("64 octets de "+ip+" : réponse reçue");L.push("1 paquet émis, 1 reçu, 0 % de perte");ok=true;}
    else{L.push(r.fwd.noRoute?"ping: sendto: No route to host":"Délai d'attente dépassé");L.push("1 paquet émis, 0 reçu, 100 % de perte");L.push("Cause : "+r.why);}
    return {lines:L,legs:legs,ok:ok};
  }
  if(action==="traceroute"||action==="tracerouteI"){
    var icmp=action==="tracerouteI";
    var f=N.send(S,from,icmp?{proto:"icmp",src:null,dst:ip,type:8}:{proto:"udp",src:null,dst:ip,sport:40000,dport:33434},false);legs.push(f.path);
    L.push("traceroute"+(icmp?" -I":"")+" vers "+t+" ("+ip+"), 30 sauts maximum");
    f.hops.forEach(function(h,i){L.push(" "+(i+1)+"  "+nameOf(h.ip));});
    if(f.ok){ok=true;}else{L.push(" "+(f.hops.length+1)+"  * * *");L.push("Cause : "+f.reason);if(!icmp&&f.blockedBy==="PC2")L.push("Avec les règles du guide, PC2 n'autorise que l'ICMP : essayez « traceroute -I ».");}
    return {lines:L,legs:legs,ok:ok};
  }
  var port=action==="smtp"?25:80;
  if(action==="proxy"){
    var squid=trip(S,from,{proto:"tcp",src:null,dst:"77.88.4.130",sport:41000,dport:3128,setup:true});legs.push.apply(legs,squid.legs);
    if(!squid.ok){L.push("Le navigateur ne peut pas joindre le proxy proxy.prune.org:3128 : "+squid.why);return {lines:L,legs:legs,ok:false};}
    L.push("Navigateur → proxy.prune.org:3128");
    if(/pirate/i.test(t+path)){L.push("HTTP/1.1 403 Forbidden (Squid, ACL \"pirate\")");L.push("Page bloquée par le proxy : l'URL contient « pirate ».");return {lines:L,legs:legs,ok:false,blocked:true};}
    var up=trip(S,"PC2",{proto:"tcp",src:null,dst:ip,sport:42000,dport:80,setup:true});legs.push.apply(legs,up.legs);
    if(!up.ok){L.push("Le proxy n'arrive pas à joindre "+ip+" : "+up.why);return {lines:L,legs:legs,ok:false};}
    L.push("Proxy → "+t+" ("+ip+"), HTTP/1.1 200 OK");if(NODES[up.dest].page)L.push("Page : « "+NODES[up.dest].page+" »");return {lines:L,legs:legs,ok:true};
  }
  var r2=trip(S,from,{proto:"tcp",src:null,dst:ip,sport:43000,dport:port,setup:true});legs.push.apply(legs,r2.legs);
  L.push((action==="smtp"?"Connexion SMTP vers ":"curl -I http://")+t+(action==="smtp"?":25":path));
  if(r2.ok){
    if(action==="smtp"){L.push("220 mail.prune.org ESMTP Postfix");}
    else{L.push("HTTP/1.1 200 OK");if(NODES[r2.dest].page)L.push("Page : « "+NODES[r2.dest].page+" »");}
    if(r2.fwd.pkt.dst!==ip)L.push("(après traduction, la requête a atteint "+r2.fwd.pkt.dst+")");
    ok=true;
  } else L.push((r2.refused?"Connexion refusée":"Connexion expirée")+" : "+r2.why);
  r2.fwd.notes.concat(r2.back?r2.back.notes:[]).forEach(function(n){L.push("· "+n);});
  return {lines:L,legs:legs,ok:ok};
}
root.Lab={run:run};
if(typeof module!=="undefined")module.exports=root.Lab;
})(typeof window!=="undefined"?window:globalThis);
