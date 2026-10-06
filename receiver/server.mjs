import http from 'node:http';
import {createHmac,timingSafeEqual} from 'node:crypto';
const secret=process.env.WEBHOOK_SECRET||'local-demo-key-change-me';
const seen=new Map();
http.createServer((req,res)=>{
 if(req.url==='/health'){res.end('ok');return;}
 if(req.url!=='/events'||req.method!=='POST'){res.writeHead(404).end();return;}
 let body='';req.on('data',c=>{body+=c;if(body.length>16384)req.destroy();});req.on('end',()=>{
  const ts=req.headers['x-timestamp'];const signature=req.headers['x-signature'];
  const expected=createHmac('sha256',secret).update(ts+'.'+body).digest('hex');
  if(!signature||!/^[a-f0-9]{64}$/.test(signature)||!ts||Math.abs(Date.now()/1000-Number(ts))>300||!Number.isFinite(Number(ts))||!timingSafeEqual(Buffer.from(signature),Buffer.from(expected))){res.writeHead(401).end();return;}
  let event;try{event=JSON.parse(body);}catch{res.writeHead(400).end();return;}
  if(event.message==='simulate-error'){res.writeHead(500).end();return;}
  if(event.message==='simulate-rate-limit'){res.writeHead(429).end();return;}
  for(const [id,time] of seen)if(Date.now()-time>3600000)seen.delete(id);
  if(seen.size>=10000){res.writeHead(503).end();return;}
  const duplicate=seen.has(event.eventId);seen.set(event.eventId,Date.now());
  res.setHeader('Content-Type','application/json');res.end(JSON.stringify({eventId:event.eventId,duplicate}));
 });
}).listen(9092,'0.0.0.0');
