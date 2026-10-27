/* Port JavaScript fidèle du moteur Java (Plateau, Mouvement, Bot) : plateau 11x11, règles propres au projet. */
(function(root){
"use strict";
var R="R",B="B";
function mk(t,x,y,c){return {t:t,x:x,y:y,c:c};}
function Board(){
  var b=[],i,j;for(i=0;i<11;i++){b.push([]);for(j=0;j<11;j++)b[i].push(null);}
  this.cases=b;this.fini=false;
  var put=function(t,x,y,c){b[x][y]=mk(t,x,y,c);};
  put("general",5,0,B);put("general",5,10,R);
  put("garde",4,0,B);put("garde",6,0,B);put("garde",4,10,R);put("garde",6,10,R);
  put("elephant",2,0,B);put("elephant",8,0,B);put("elephant",2,10,R);put("elephant",8,10,R);
  put("tour",0,0,B);put("tour",10,0,B);put("tour",0,10,R);put("tour",10,10,R);
  put("cheval",1,0,B);put("cheval",9,0,B);put("cheval",1,10,R);put("cheval",9,10,R);
  put("canon",1,2,B);put("canon",9,2,B);put("canon",1,8,R);put("canon",9,8,R);
  [0,2,5,8,10].forEach(function(x){put("soldat",x,3,B);put("soldat",x,7,R);});
}
var P=Board.prototype;
P.get=function(x,y){return(x<0||x>=11||y<0||y>=11)?null:this.cases[x][y];};
P.set=function(x,y,p){if(x>=0&&x<11&&y>=0&&y<11)this.cases[x][y]=p;};
var abs=Math.abs;
function valide(p,nx,ny,pl){
  var x=p.x,y=p.y,t=p.t;
  if(t==="soldat")return soldat(p,x,y,nx,ny);
  if(t==="general")return abs(nx-x)+abs(ny-y)===1&&nx>=3&&nx<=7&&(ny<=2||ny>=8);
  if(t==="tour")return tour(p,x,y,nx,ny,pl);
  if(t==="canon")return canon(p,x,y,nx,ny,pl);
  if(t==="cheval")return cheval(x,y,nx,ny,pl);
  if(t==="elephant")return elephant(p,x,y,nx,ny);
  if(t==="garde")return abs(nx-x)===1&&abs(ny-y)===1&&nx>=3&&nx<=7&&(ny<=2||ny>=8);
  return false;
}
function cheval(x,y,nx,ny,pl){
  if(ny===5)return false;
  var trav=(y===4&&ny===6)||(y===6&&ny===4)||(y===4&&ny===7)||(y===6&&ny===3);
  var spec=(y===3&&ny===6)||(y===7&&ny===4);
  var norm=(abs(nx-x)===2&&abs(ny-y)===1)||(abs(nx-x)===1&&abs(ny-y)===2);
  if(trav)return(abs(nx-x)===2&&abs(ny-y)===2)||(abs(nx-x)===1&&abs(ny-y)===3);
  if(spec)return(abs(nx-x)===2&&abs(ny-y)===1)||(abs(nx-x)===1&&abs(ny-y)===3);
  if(norm){
    if(abs(nx-x)===2){var xo=nx>x?x+1:x-1;if(pl[xo][y]!==null)return false;}
    if(abs(ny-y)===2){var yo=ny>y?y+1:y-1;if(pl[x][yo]!==null)return false;}
    return true;
  }
  return false;
}
var EN={B:[[2,0],[2,4],[8,0],[8,4],[0,2],[5,2],[10,2]],R:[[2,10],[2,6],[8,10],[8,6],[0,8],[5,8],[10,8]]};
function elephant(p,x,y,nx,ny){
  var ok=EN[p.c].some(function(c){return c[0]===nx&&c[1]===ny;});
  if(!ok)return false;
  if(abs(nx-x)>3||abs(ny-y)>2)return false;
  return true;
}
function soldat(p,x,y,nx,ny){
  if(ny===5)return false;
  if(p.c===B){
    if(y===4&&ny===6&&nx===x)return true;
    if(y<=4)return nx===x&&ny===y+1;
    return(nx===x&&ny===y+1)||(abs(nx-x)===1&&ny===y);
  }
  if(y===6&&ny===4&&nx===x)return true;
  if(y>=6)return nx===x&&ny===y-1;
  return(nx===x&&ny===y-1)||(abs(nx-x)===1&&ny===y);
}
function tour(p,x,y,nx,ny,pl){
  var i,s;
  if(ny===5)return false;
  if(nx===x){s=ny>y?1:-1;for(i=y+s;i!==ny;i+=s){if(i<0||i>=11)return false;if(i===5)continue;if(pl[x][i]!==null)return false;}}
  else if(ny===y){s=nx>x?1:-1;for(i=x+s;i!==nx;i+=s){if(i<0||i>=11)return false;if(pl[i][y]!==null)return false;}}
  else return false;
  if(pl[nx][ny]!==null&&pl[nx][ny].c===p.c)return false;
  return true;
}
function canon(p,x,y,nx,ny,pl){
  if(ny===5)return false;
  if(x!==nx&&y!==ny)return false;
  var ob=0,i,s;
  if(x===nx){s=ny>y?1:-1;for(i=y+s;i!==ny;i+=s){if(i===5)continue;if(i<0||i>=11)return false;if(pl[x][i]!==null)ob++;}}
  else if(y===ny){s=nx>x?1:-1;for(i=x+s;i!==nx;i+=s){if(i<0||i>=11)return false;if(pl[i][y]!==null)ob++;}}
  if(pl[nx][ny]!==null&&pl[nx][ny].c===p.c)return false;
  if(pl[nx][ny]===null)return ob===0;
  return ob===1;
}
P.valide=function(p,nx,ny){return valide(p,nx,ny,this.cases);};
P.canonAttaque=function(c,xG,yG){
  var xC=c.x,yC=c.y;
  if(xC!==xG&&yC!==yG)return false;
  var dx=Math.sign(xG-xC),dy=Math.sign(yG-yC),ec=0,x=xC+dx,y=yC+dy;
  while(x!==xG||y!==yG){if(!(x===xC&&y===5)&&this.get(x,y)!==null)ec++;x+=dx;y+=dy;}
  var cb=this.get(xG,yG);
  return ec===1&&cb!==null&&cb.t==="general"&&cb.c!==c.c;
};
P.enEchec=function(c){
  var rx=-1,ry=-1,x,y,p;
  for(x=0;x<11;x++){for(y=0;y<11;y++){p=this.get(x,y);if(p&&p.t==="general"&&p.c===c){rx=x;ry=y;break;}}if(rx!==-1)break;}
  var adv=c===R?B:R;
  for(x=0;x<11;x++)for(y=0;y<11;y++){
    var a=this.get(x,y);if(!a||a.c!==adv)continue;
    if(a.t==="canon"){if(this.canonAttaque(a,rx,ry))return true;}
    else if(valide(a,rx,ry,this.cases))return true;
  }
  return false;
};
P.metEnEchec=function(piece,x,y,nx,ny){
  var cap=this.get(nx,ny);
  this.set(nx,ny,piece);this.set(x,y,null);
  var e=this.enEchec(piece.c);
  this.set(x,y,piece);this.set(nx,ny,cap);
  return e;
};
P.existeSauvant=function(c){
  var x,y,i,j;
  for(x=0;x<11;x++)for(y=0;y<11;y++){var p=this.get(x,y);if(p&&p.c===c){
    for(i=0;i<11;i++)for(j=0;j<11;j++){
      if(!valide(p,i,j,this.cases))continue;
      var cb=this.get(i,j);this.set(i,j,p);this.set(x,y,null);
      var e=this.enEchec(c);this.set(x,y,p);this.set(i,j,cb);
      if(!e)return true;
    }}}
  return false;
};
P.echecEtMat=function(c){return this.enEchec(c)&&!this.existeSauvant(c);};
P.pat=function(c){
  if(this.enEchec(c))return false;
  var x,y,i,j;
  for(x=0;x<11;x++)for(y=0;y<11;y++){var p=this.get(x,y);if(p&&p.c===c)for(i=0;i<11;i++)for(j=0;j<11;j++)if(valide(p,i,j,this.cases))return false;}
  return true;
};
P.faceAFace=function(){
  var rx=-1,ry=-1,bx=-1,by=-1,x,y;
  for(x=0;x<11;x++)for(y=0;y<11;y++){var p=this.get(x,y);if(p&&p.t==="general"){if(p.c===R){rx=x;ry=y;}else{bx=x;by=y;}}}
  if(rx===bx){var mn=Math.min(ry,by),mx=Math.max(ry,by);for(y=mn+1;y<mx;y++)if(this.get(rx,y)!==null)return false;return true;}
  return false;
};
/* Plateau.deplacerPiece : retourne {ok, fin} ou null */
P.deplacer=function(x,y,nx,ny){
  if(this.fini)return null;
  var p=this.get(x,y),capt=this.get(nx,ny),msg=null;
  if(!p)return null;
  if(this.metEnEchec(p,x,y,nx,ny)){this.set(nx,ny,capt);return null;}
  this.set(nx,ny,p);p.x=nx;p.y=ny;this.set(x,y,null);
  if(this.faceAFace()){msg="Les généraux sont face à face : la partie est terminée.";this.fini=true;}
  else if(this.echecEtMat(p.c)){msg="La partie est terminée : échec et mat.";this.fini=true;}
  else if(this.pat(p.c)){msg="La partie est terminée : pat.";this.fini=true;}
  return {ok:true,fin:msg};
};
/* Mouvement.effectuerMouvement : null si refusé, {refuse} si le général serait en échec, sinon {echec,mat,fin} */
P.jouer=function(p,x,y,nx,ny){
  if(!p||this.fini)return null;
  if(!valide(p,nx,ny,this.cases))return null;
  var d=this.get(nx,ny);if(d&&d.c===p.c)return null;
  var cap=this.get(nx,ny);this.set(nx,ny,p);this.set(x,y,null);
  var met=this.enEchec(p.c);this.set(x,y,p);this.set(nx,ny,cap);
  if(met)return {refuse:"general"};
  var r=this.deplacer(x,y,nx,ny);
  var adv=p.c===R?B:R,out={echec:false,mat:false,fin:r&&r.fin};
  if(this.echecEtMat(adv)){out.mat=true;this.fini=true;}
  else if(this.enEchec(adv))out.echec=true;
  return out;
};
var VAL={soldat:1,garde:2,elephant:2,cheval:4,canon:5,tour:9,general:1000};
function Bot(pl){this.pl=pl;this.last=null;}
Bot.prototype.mouvements=function(p){var n=0,x,y;for(x=0;x<11;x++)for(y=0;y<11;y++)if(valide(p,x,y,this.pl.cases))n++;return n;};
Bot.prototype.menaces=function(p){var n=0,x,y;for(x=0;x<11;x++)for(y=0;y<11;y++){var a=this.pl.get(x,y);if(a&&a.c!==B&&valide(p,x,y,this.pl.cases))n++;}return n;};
Bot.prototype.menacesAdv=function(x0,y0){var n=0,x,y;for(x=0;x<11;x++)for(y=0;y<11;y++){var p=this.pl.get(x,y);if(p&&p.c===R&&valide(p,x0,y0,this.pl.cases))n++;}return n;};
Bot.prototype.meilleur=function(){
  var pl=this.pl,best=-Infinity,bc=null,gx=-1,gy=-1,i,j,x0,y0,x1,y1;
  for(i=0;i<11;i++)for(j=0;j<11;j++){var t=pl.get(i,j);if(t&&t.t==="general"&&t.c===B){gx=i;gy=j;}}
  for(x0=0;x0<11;x0++)for(y0=0;y0<11;y0++){
    var p=pl.get(x0,y0);if(!p||p.c!==B)continue;
    for(x1=0;x1<11;x1++)for(y1=0;y1<11;y1++){
      var cible=pl.get(x1,y1);
      if(cible&&cible.c===B)continue;
      if(!valide(p,x1,y1,pl.cases))continue;
      pl.set(x1,y1,p);pl.set(x0,y0,null);
      if(pl.metEnEchec(p,x0,y0,x1,y1)){pl.set(x0,y0,p);pl.set(x1,y1,cible);continue;}
      var s=0;
      if(cible)s+=10*(VAL[cible.t]||0);
      if(p.t==="general"&&!cible)s-=5;
      if(p.t==="soldat"){var dy=y1-y0;if(dy>0)s+=2*dy;}
      s+=this.mouvements(p);
      s+=3*this.menaces(p);
      if(abs(x1-gx)+abs(y1-gy)===1)s+=5;
      if(this.last&&this.last[0]===x1&&this.last[1]===y1&&this.last[2]===x0&&this.last[3]===y0)s-=15;
      s-=15*this.menacesAdv(x1,y1);
      pl.set(x0,y0,p);pl.set(x1,y1,cible);
      if(s>best){best=s;bc=[x0,y0,x1,y1];}
    }
  }
  return bc;
};
Bot.prototype.jouer=function(){
  if(this.pl.fini)return null;
  var c=this.meilleur();
  if(c){var r=this.pl.deplacer(c[0],c[1],c[2],c[3]);this.last=c;return {coup:c,fin:r&&r.fin};}
  return null;
};
root.XQ={Board:Board,Bot:Bot,notation:function(x,y){return String.fromCharCode(65+x)+(11-y);}};
if(typeof module!=="undefined")module.exports=root.XQ;
})(typeof window!=="undefined"?window:globalThis);
