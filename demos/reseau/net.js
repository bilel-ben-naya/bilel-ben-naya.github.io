/* Modèle du TP de déploiement réseau (groupe prune.org) : adresses, routes, NAT/PAT et règles ipfw du guide. */
(function(root){
"use strict";
function ip2n(ip){return ip.split(".").reduce(function(a,b){return a*256+(+b);},0);}
function inNet(ip,cidr){var p=cidr.split("/"),m=+p[1],bits=m===0?0:(0xFFFFFFFF<<(32-m))>>>0;return ((ip2n(ip)&bits)>>>0)===((ip2n(p[0])&bits)>>>0);}
var NET={WAN:"200.222.14.188/30",DMZ:"77.88.4.128/27",INV:"77.88.4.160/27",PRIV:"192.168.22.0/24",EXT:"5.5.5.0/24"};
var NODES={
 EXT:{label:"Internet (pigeon.canard.com)",host:"pigeon.canard.com",ifs:[{n:"em0",ip:"5.5.5.5",net:NET.EXT}],extra:["8.8.8.8"],routes:[{net:"0.0.0.0/0",via:"5.5.5.1"}],fwd:false,svc:{tcp:[80],udp:[]},page:"pigeon.canard.com"},
 ISP:{label:"Routeur FAI",host:"fai",ifs:[{n:"wan",ip:"200.222.14.190",net:NET.WAN},{n:"net",ip:"5.5.5.1",net:NET.EXT}],routes:[{net:"77.88.4.128/26",via:"200.222.14.189"},{net:"0.0.0.0/0",via:"5.5.5.5"}],fwd:true,svc:{tcp:[],udp:[]}},
 PC1:{label:"PC1",host:"router.prune.org",role:"Routeur FAI/DMZ, mail",ifs:[{n:"em0",ip:"200.222.14.189",net:NET.WAN},{n:"em1",ip:"77.88.4.129",net:NET.DMZ}],routes:[{net:"0.0.0.0/0",via:"200.222.14.190"},{net:NET.INV,via:"77.88.4.130"}],fwd:true,svc:{tcp:[25,110],udp:[]}},
 PC2:{label:"PC2",host:"dns1.prune.org",role:"Routeur DMZ/invité, DNS primaire, proxy",ifs:[{n:"em0",ip:"77.88.4.161",net:NET.INV},{n:"em1",ip:"77.88.4.130",net:NET.DMZ}],routes:[{net:"0.0.0.0/0",via:"77.88.4.129"}],fwd:true,svc:{tcp:[3128,53],udp:[53]}},
 PC3:{label:"PC3",host:"web.prune.org",role:"Serveur web public, DNS secondaire",ifs:[{n:"em1",ip:"77.88.4.131",net:NET.DMZ}],routes:[{net:"0.0.0.0/0",via:"77.88.4.129"}],fwd:false,svc:{tcp:[80,53],udp:[53]},page:"Bienvenue sur prune.org"},
 PC4:{label:"PC4",host:"invite.prune.org",role:"Poste invité",ifs:[{n:"em0",ip:"77.88.4.162",net:NET.INV}],routes:[{net:"0.0.0.0/0",via:"77.88.4.161"}],fwd:false,svc:{tcp:[],udp:[]}},
 PC5:{label:"PC5",host:"nat.prune.org",role:"Routeur invité/privé, NAT et PAT",ifs:[{n:"em0",ip:"77.88.4.163",net:NET.INV},{n:"em1",ip:"192.168.22.1",net:NET.PRIV}],routes:[{net:"0.0.0.0/0",via:"77.88.4.161"}],fwd:true,svc:{tcp:[],udp:[]}},
 PC6:{label:"PC6",host:"privsrv.prune.org",role:"Intranet et DHCP",ifs:[{n:"em1",ip:"192.168.22.2",net:NET.PRIV}],routes:[{net:"0.0.0.0/0",via:"192.168.22.1"}],fwd:false,svc:{tcp:[80],udp:[]},page:"Page web du réseau privé"}
};
var ZONE={"router.prune.org":"77.88.4.129","mail.prune.org":"77.88.4.129","dns1.prune.org":"77.88.4.130","proxy.prune.org":"77.88.4.130","web.prune.org":"77.88.4.131","dns2.prune.org":"77.88.4.131","invite.prune.org":"77.88.4.162","nat.prune.org":"77.88.4.163","privsrv.prune.org":"192.168.22.2","pigeon.canard.com":"5.5.5.5"};
var CNAME={"www.prune.org":"web.prune.org","www-intranet.prune.org":"nat.prune.org"};
function resolve(name){name=name.toLowerCase().replace(/\.$/,"");var c=null;if(CNAME[name]){c=CNAME[name];name=c;}return ZONE[name]?{ip:ZONE[name],cname:c,name:name}:null;}
function ptr(ip){var r=null;Object.keys(ZONE).forEach(function(k){if(ZONE[k]===ip&&!r&&k!=="mail.prune.org"&&k!=="proxy.prune.org"&&k!=="dns2.prune.org")r=k;});return ip==="77.88.4.131"?"web.prune.org":r;}

/* Pare-feu ipfw (règles du guide, première règle qui correspond). */
function R(n,act,m){return {n:n,act:act,m:m};}
var priv=function(ip){return inNet(ip,"192.168.0.0/16")||inNet(ip,"10.0.0.0/8")||inNet(ip,"172.16.0.0/12");};
var FW={
 PC1:[
  R(22,"deny",function(p,i){return inNet(p.src,"77.88.4.128/26")&&i==="em0";}),
  R(23,"deny",function(p,i){return inNet(p.src,"192.168.0.0/16")&&i==="em0";}),
  R(24,"deny",function(p,i){return inNet(p.src,"10.0.0.0/8")&&i==="em0";}),
  R(25,"deny",function(p,i){return inNet(p.src,"172.16.0.0/12")&&i==="em0";}),
  R(26,"deny",function(p,i){return p.src==="200.222.14.189"&&i==="em1";}),
  R(40,"allow",function(p){return p.proto==="tcp"&&!p.setup;}),
  R(50,"allow",function(p){return p.proto==="tcp"&&p.dst==="77.88.4.131"&&p.dport===80&&p.setup;}),
  R(70,"allow",function(p){return p.proto==="tcp"&&p.dst==="77.88.4.129"&&p.dport===25&&p.setup;}),
  R(80,"allow",function(p){return p.proto==="tcp"&&p.dst==="77.88.4.129"&&p.dport===110&&p.setup;}),
  R(130,"allow",function(p){return p.proto==="udp"&&p.dport===53&&p.dst==="77.88.4.130"&&p.sport===53;}),
  R(131,"allow",function(p){return p.proto==="udp"&&p.src==="77.88.4.130"&&p.sport===53;}),
  R(134,"allow",function(p){return p.proto==="udp"&&p.dport===53&&p.dst==="77.88.4.131"&&p.sport===53;}),
  R(135,"allow",function(p){return p.proto==="udp"&&p.src==="77.88.4.131"&&p.sport===53;}),
  R(150,"allow",function(p){return p.proto==="tcp"&&inNet(p.src,NET.DMZ)&&p.setup;}),
  R(150.1,"allow",function(p){return p.proto==="udp"&&inNet(p.src,NET.DMZ);}),
  R(151,"allow",function(p){return p.proto==="icmp"&&inNet(p.src,NET.DMZ);}),
  R(300,"allow",function(p){return p.proto==="icmp"&&inNet(p.src,"77.88.4.128/26")&&p.type===8;}),
  R(301,"allow",function(p){return p.proto==="icmp"&&inNet(p.dst,"77.88.4.128/26")&&p.type===0;}),
  R(302,"allow",function(p){return p.proto==="udp"&&inNet(p.src,"77.88.4.128/26")&&p.dport>=33434&&p.dport<=33534;}),
  R(303,"allow",function(p){return p.proto==="icmp"&&inNet(p.dst,"77.88.4.128/26")&&(p.type===3||p.type===11);}),
  R(65000,"deny",function(){return true;})
 ],
 PC2:[
  R(20,"deny",function(p,i){return inNet(p.src,NET.INV)&&i==="em1";}),
  R(21,"deny",function(p,i){return inNet(p.src,"192.168.0.0/16")&&i==="em1";}),
  R(22,"deny",function(p,i){return inNet(p.src,"10.0.0.0/8")&&i==="em1";}),
  R(23,"deny",function(p,i){return inNet(p.src,"172.16.0.0/12")&&i==="em1";}),
  R(24,"deny",function(p,i){return p.src==="77.88.4.129"&&i==="em0";}),
  R(25,"deny",function(p,i){return p.src==="77.88.4.129"&&i==="em1"&&p.dst==="77.88.4.129";}),
  R(40,"allow",function(p){return p.proto==="icmp";}),
  R(50,"allow",function(p){return p.proto==="tcp"&&!p.setup;}),
  R(60,"allow",function(p){return p.proto==="tcp"&&inNet(p.src,NET.INV)&&p.dst==="77.88.4.130"&&p.dport===3128&&p.setup;}),
  R(70,"allow",function(p){return p.proto==="tcp"&&inNet(p.src,NET.INV)&&p.dst==="77.88.4.129"&&p.dport===25&&p.setup;}),
  R(80,"allow",function(p){return p.proto==="tcp"&&inNet(p.src,NET.INV)&&p.dst==="77.88.4.129"&&p.dport===110&&p.setup;}),
  R(90,"allow",function(p){return p.proto==="tcp"&&inNet(p.src,NET.INV)&&p.dst==="77.88.4.131"&&p.dport===80&&p.setup;}),
  R(100,"allow",function(p){return p.proto==="udp"&&inNet(p.src,NET.INV)&&(p.dst==="77.88.4.130"||p.dst==="77.88.4.131")&&p.dport===53;}),
  R(101,"allow",function(p){return p.proto==="udp"&&(p.src==="77.88.4.130"||p.src==="77.88.4.131")&&p.sport===53&&inNet(p.dst,NET.INV);}),
  R(110,"allow",function(p){return p.proto==="tcp"&&inNet(p.src,NET.INV)&&p.dst==="77.88.4.163"&&p.dport===80&&p.setup;}),
  R(65000,"deny",function(){return true;})
 ]
};

function fresh(){var S={opts:{pc2fwd:true,pc5fwd:true,nat:true,pat:true,fw1:true,fw2:true,dns1:true},nat:[]};return S;}
function ownerOf(ip){var k,i,n;for(k in NODES){n=NODES[k];for(i=0;i<n.ifs.length;i++)if(n.ifs[i].ip===ip)return k;if(n.extra&&n.extra.indexOf(ip)>-1)return k;}return null;}
function routeLookup(node,dst){
  var best=null,bl=-1,rs=[],n=NODES[node];
  n.ifs.forEach(function(f){rs.push({net:f.net,via:null,dev:f.n});});
  n.routes.forEach(function(r){var dev=null;n.ifs.forEach(function(f){if(inNet(r.via,f.net))dev=f.n;});rs.push({net:r.net,via:r.via,dev:dev});});
  rs.forEach(function(r){if(inNet(dst,r.net)){var l=+r.net.split("/")[1];if(l>bl){bl=l;best=r;}}});
  return best;
}
function ifaceOn(node,net){var f=NODES[node].ifs.filter(function(x){return x.net===net;})[0];return f?f.n:NODES[node].ifs[0].n;}
function ifaceByName(node,n){return NODES[node].ifs.filter(function(x){return x.n===n;})[0];}

/* Envoie un paquet de `from` vers pkt.dst. Retourne {ok, path:[noeuds], notes:[], reason, lastNode, pkt}. */
function send(S,from,pkt,reply){
  var cur=from,path=[from],notes=[],hops=[],inIf=null,prevNet=null,guard=0;
  pkt=JSON.parse(JSON.stringify(pkt));
  var out=function(ok,reason,extra){var o={ok:ok,path:path,notes:notes,hops:hops,reason:reason||"",pkt:pkt,at:cur};if(extra)for(var k in extra)o[k]=extra[k];return o;};
  while(guard++<12){
    var node=NODES[cur];
    if(cur!==from){
      if(prevNet){var f=node.ifs.filter(function(x){return x.net===prevNet;})[0];inIf=f?f.n:node.ifs[0].n;hops.push({node:cur,ip:ifaceByName(cur,inIf).ip});}
      /* pare-feu */
      if(FW[cur]&&((cur==="PC1"&&S.opts.fw1)||(cur==="PC2"&&S.opts.fw2))){
        var rules=FW[cur],hit=null;for(var i=0;i<rules.length;i++){if(rules[i].m(pkt,inIf)){hit=rules[i];break;}}
        if(hit&&hit.act==="deny") return out(false,"bloqué par le pare-feu de "+cur+" (règle ipfw "+Math.floor(hit.n)+(hit.n===65000?", refus par défaut":"")+")",{blockedBy:cur,fw:true});
        if(hit)notes.push(cur+" : règle ipfw "+Math.floor(hit.n)+" autorise");
      }
      /* réponse sur NAT / PAT */
      if(cur==="PC5"){
        if(reply){
          var m=S.nat.filter(function(e){return e.kind==="snat"&&pkt.dst===e.pub&&pkt.src===e.peer;})[0];
          if(m){pkt.dst=m.orig;notes.push("PC5 : traduction inverse "+e2(m.pub)+" → "+m.orig);}
          var d=S.nat.filter(function(e){return e.kind==="dnat"&&pkt.src===e.priv;})[0];
          if(d){pkt.src=d.pub;notes.push("PC5 : la réponse repart avec l'adresse publique "+d.pub);}
        } else if(S.opts.pat&&pkt.proto==="tcp"&&pkt.dport===80&&pkt.dst==="77.88.4.163"&&inIf==="em0"){
          pkt.dst="192.168.22.2";S.nat.push({kind:"dnat",pub:"77.88.4.163",priv:"192.168.22.2"});notes.push("PC5 : PAT, 77.88.4.163:80 redirigé vers 192.168.22.2:80");
        }
      }
    }
    if(ownerOf(pkt.dst)===cur) return out(true,"",{arrived:cur});
    if(cur==="ISP"&&priv(pkt.dst)) return out(false,"adresse privée non routable sur Internet",{blockedBy:"ISP"});
    if(cur==="ISP"&&priv(pkt.src)) return out(false,"adresse source privée rejetée par le FAI",{blockedBy:"ISP"});
    if(cur!==from){
      var okf=node.fwd&&!(cur==="PC2"&&!S.opts.pc2fwd)&&!(cur==="PC5"&&!S.opts.pc5fwd);
      if(!okf) return out(false,"routage IP désactivé sur "+cur+" (net.inet.ip.forwarding=0)",{blockedBy:cur});
    }
    var rt=routeLookup(cur,pkt.dst);
    if(!rt) return out(false,"aucune route vers "+pkt.dst+" depuis "+cur,{noRoute:true,blockedBy:cur});
    if(!pkt.src){pkt.src=ifaceByName(cur,rt.dev).ip;}
    if(cur==="PC5"&&S.opts.nat&&!reply&&rt.dev==="em0"&&inNet(pkt.src,NET.PRIV)){
      S.nat.push({kind:"snat",orig:pkt.src,pub:"77.88.4.163",peer:pkt.dst});notes.push("PC5 : NAT, source "+pkt.src+" remplacée par 77.88.4.163");pkt.src="77.88.4.163";
    }
    var nh=rt.via||pkt.dst,nx=ownerOf(nh);
    if(!nx) return out(false,"hôte "+nh+" injoignable (aucune réponse ARP)",{blockedBy:cur});
    var egress=ifaceByName(cur,rt.dev);prevNet=egress.net;cur=nx;path.push(cur);
  }
  return out(false,"TTL dépassé");
}
function e2(x){return x;}
root.Net={NODES:NODES,NET:NET,ZONE:ZONE,CNAME:CNAME,resolve:resolve,ptr:ptr,fresh:fresh,send:send,ownerOf:ownerOf,inNet:inNet};
if(typeof module!=="undefined")module.exports=root.Net;
})(typeof window!=="undefined"?window:globalThis);
