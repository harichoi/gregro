import java.awt.*;	
import java.awt.event.*;	
import javax.swing.*;	
import java.util.*;	
import java.io.*;	
import java.net.*;	
import java.awt.geom.Ellipse2D;	

public class GREGRO extends JPanel implements KeyListener, MouseListener, MouseMotionListener{	
	Socket socket=null;
	ServerSocket ssocket=null;
	InputStream ins=null;
	OutputStream outs=null;
	int _BASIC=1;
	int _SUPPLY=2;
	int _KNIFE=3;
	int _CREATOR=4;
	int _FARMER=5;
	int _NORMALTOWER=6;
	int _ATANK=7;
	int _STMAN=8;
	int ME=1;
	int _MOVE=1;
	int _HOLD=2;
	int _MOVEMAKE=3;
	int _ATTACK=4;
	int _CATCH=5;
	int _MAKE=6;
	int _UPGRADE=7;
	int _CHSTATE=8;
	int _DESTROY=9;
	int realnumber=1;
	GREGRO g;
	int sz, selarea=0;
	int flowtime;
	int mpr;
	TEAM player[]=new TEAM[100];
	COPA copa;
	AREA are[];
	Vector<OBJ> obj;
	Vector<OBJ> sel;
	Vector <ORDER> order;
	Vector <ORDER> beforder;
	int keyboard[];
	int map[][];//-1 : sea
	int area[][];
	int mode;//'S',0 : empty  'C' : create  'M' : move  'B' : building
	int mx,my,mmx,mmy;
	int defx,defy;
	Color ctable[]=new Color[10];
	void connect(String ip){	
		try{
		socket=new Socket(ip,9000);
		ins=socket.getInputStream();
		outs=socket.getOutputStream();
		}catch(Exception exc){System.out.println(exc);}
		ME=1;
	}	
	void listen(){	
		try{
		ssocket=new ServerSocket(9000);
		socket=ssocket.accept();
		ins=socket.getInputStream();
		outs=socket.getOutputStream();
		}catch(Exception exc){System.out.println(exc);}
		ME=2;
	}	
	double rand(){	
		if(socket==null) return Math.random();
		int oo=flowtime/100;
		oo=oo%10;
		return (double)oo/10.0;
	}	
	int getmoney(int type){	
		if(type==_STMAN)return 1600;
		if(type==_ATANK)return 2000;
		return 1000;
	}	
	int getnpop(int type){	
		if(type==_FARMER) return 2;
		if(type==_KNIFE) return 2;
		if(type==_SUPPLY) return 5;
		if(type==_BASIC) return 3;
		if(type==_CREATOR) return -30;	
		if(type==_NORMALTOWER) return 2;	
		if(type==_ATANK) return 5;	
		if(type==_STMAN) return 3;	
		return 0;	
	}		
	int getmapxy(double x,double y){		
		return map[(int)(x/sz)][(int)(y/sz)];	
	}		
	int getareaxy(double x,double y){		
		return area[(int)(x/sz)][(int)(y/sz)];	
	}		
	OBJ unitmake(int type,double x,double y,int team){		
		OBJ r=null;	
		if(player[team].money>=getmoney(type) && getmapxy(x,y)>=0 && player[team].npop>=getnpop(type)){	
			player[team].money-=getmoney(type);
			player[team].npop-=getnpop(type);
			if(type==_FARMER){	
				r=new FARMER(x,y,team);
			} else if(type==_BASIC){	
				r=new BASIC(x,y,team);
			} else if(type==_KNIFE){	
				r=new KNIFE(x,y,team);
			} else if(type==_ATANK){	
				r=new ATANK(x,y,team);
			} else if(type==_STMAN){	
				r=new STMAN(x,y,team);
			}		
			if(r!=null)	
			obj.add(r);	
		}		
		return r;		
	}			
	OBJ buildmake(int type,double x,double y,int team){			
		OBJ r=null;		
		int i,nums=0;		
		if(type==_CREATOR){		
		for(i=0 ; i<obj.size() ; i++)		
			if(obj.get(i).type==_CREATOR && obj.get(i).team==team) nums++;	
			if(nums>5)	
				return null;
		}		
		if(getmapxy(x,y)==0 && player[team].money>=getmoney(type) && player[team].npop>=getnpop(type)){		
			player[team].money-=getmoney(type);	
			player[team].npop-=getnpop(type);	
			if(type==_SUPPLY){	
				r=new SUPPLY(x,y,team);
			} else if(type==_CREATOR){	
				r=new CREATOR(x,y,team);
			} else if(type==_NORMALTOWER){	
				r=new NORMALTOWER(x,y,team);
			}
			if(r!=null)
			obj.add(r);
		}	
		return r;	
	}		
	public void recvorder(InputStream in){		
		try{	
		DataInputStream dis=new DataInputStream(in);	
		int n=dis.readInt();	
		int i;	
		for(i=0 ; i<n ; i++){	
			ORDER ord=new ORDER();
			ord.input(in);
			beforder.add(ord);
		}	
		}catch(Exception exc){}		

	}			
	public void sendorder(OutputStream out){			
		try{		
			DataOutputStream dos=new DataOutputStream(out);	
			int n=0;	
			int i;	
			for(i=0 ; i<beforder.size() ; i++){	
				if(beforder.get(i).team==ME) n++;
			}	
			dos.writeInt(n);	
			for(i=0 ; i<beforder.size() ; i++){	
				if(beforder.get(i).team==ME) beforder.get(i).output(out);
			}	
		}catch(Exception exc){}		
	}		
	class ORDER{		
		int team=0;	
		int ftime=0;	
		int what=0;	
		int number=0;	
		double d1=0,d2=0;	
		int i1=0;	
		ORDER(int team){	
			this.team=team;
		}	
		ORDER(){}	
		void set(int ft,int num){	
			ftime=ft;
			number=num;
		}	
		void move(double x,double y){	
			what=_ATTACK;
			d1=x;d2=y;
		}	
		void movemake(double x,double y,int wh){	
			what=_MOVEMAKE;
			d1=x;d2=y;i1=wh;
		}	
		int making(int wh){	
			what=_MAKE;
			i1=wh;
			return 1;
		}	
		void setstate(int state){	
			what=_CHSTATE;
			i1=state;
		}				
		void catchs(){				
			what=_CATCH;			
		}				
		void destroyme(){				
			what=_DESTROY;			
		}				
		void upgrade(){				
			what=_UPGRADE;			
		}				

		void excute(){				
			System.out.println("ORDER team :"+team+" what :"+what);			
			int i;			
			for(i=0 ; i<obj.size() ; i++)			
				if(obj.get(i).number==number){		
					OBJ tar=obj.get(i);	
					if(what==_ATTACK){	
						tar.move(d1,d2);
					} else if(what==_MOVEMAKE){	
						tar.movemake(d1,d2,i1);
					} else if(what==_MAKE){	
						tar.making(i1);
					}	
					else if(what==_CATCH){	
						if(tar.type==_FARMER){
						((FARMER)tar).flag=0;
						((FARMER)tar).state=_CATCH;
						}
					} else if(what==_UPGRADE){	
						tar.upgrade();
					} else if(what==_CHSTATE){	
						tar.setstate(i1);
					} else if(what==_DESTROY){	
						tar.destroyme();
					}	
					break;	
				}		
		}				

public void input(InputStream in)
{
DataInputStream dis=new DataInputStream(in);
try{
ftime=dis.readInt();
team=dis.readInt();
what=dis.readInt();
number=dis.readInt();
i1=dis.readInt();
d1=dis.readDouble();
d2=dis.readDouble();
}catch(Exception ex){}
}
public void output(OutputStream out)
{
DataOutputStream dos=new DataOutputStream(out);		
try{		
dos.writeInt(ftime);		
dos.writeInt(team);		
dos.writeInt(what);		
dos.writeInt(number);		
dos.writeInt(i1);		
dos.writeDouble(d1);		
dos.writeDouble(d2);		
}catch(Exception ex){}		
}		
	};	
	public class TEAM{	
		int wwn;
		int supn;
		int npop;
		int next;	
		int who;	
		int money;	
		int maxtan;	
		int tan;	

		TEAM(int who){	
			npop=0;
			supn=0;
			wwn=0;
			next=0;
			if(who<=2)
			money=10000;
			else money=9999999;
			this.who=who;
			maxtan=0;
			tan=0;
		}	
		void computerAI(){		

			int i, j, k,nnext;	
			int num[]=new int[100];	
			for(i=0 ; i<100 ; i++)num[i]=0;	
			for(i=0 ; i<obj.size() ; i++){	
			if(obj.get(i).team==who){	
			num[obj.get(i).type]++;	
			}	
			}	
			if(flowtime%100==0){	
			System.out.println("");	
			for(i=0 ; i<7 ; i++)	
				System.out.print(num[i]+"/");
			}	

			//creator 1 : farmer 5 , supply 2			
			if(flowtime%100!=0)return;			
			if(npop<6){ //creator increase			
				for(i=0 ; i<obj.size() ; i++){		
					if(obj.get(i).type==_FARMER && obj.get(i).state!=_MOVEMAKE && obj.get(i).team==who){	
						ORDER ord=new ORDER(who);
						ord.set(flowtime+1,obj.get(i).number);
						ord.movemake(obj.get(i).x+rand()*600-300,obj.get(i).y+rand()*600-300,_CREATOR);
						order.add(ord);
						break;
					}	
				}		
			}			
			else if(num[_CREATOR]*1+10>num[_FARMER]){			
			//	System.out.println("num "+num[_CREATOR]*5+" "+num[_FARMER]);		
				for(i=0 ; i<obj.size() ; i++){		
					if(obj.get(i).type==_CREATOR && obj.get(i).team==who){	
						ORDER ord=new ORDER(who);
						ord.set(flowtime+1,obj.get(i).number);
						ord.making(_FARMER);
						order.add(ord);
						ORDER ord2=new ORDER(who);
						ord2.set(flowtime+1,obj.get(i).number);
						ord2.upgrade();
						if(obj.get(i).level==1)
						order.add(ord2);

					}	
				}		
			} else if(num[_CREATOR]*1>num[_SUPPLY]){			
				System.out.println("SUPPLY");		
				for(i=0 ; i<obj.size() ; i++){		
					if(obj.get(i).type==_FARMER && obj.get(i).state!=_MOVEMAKE && obj.get(i).team==who){	
						ORDER ord=new ORDER(who);
						ord.set(flowtime+1,obj.get(i).number);
						ord.movemake(obj.get(i).x+rand()*600-300,obj.get(i).y+rand()*600-300,_SUPPLY);
						order.add(ord);
				System.out.println(ord.d1+" "+ord.d2);		
						break;
					}	
				}		
			} else if(num[_CREATOR]*1+2>num[_NORMALTOWER]){			

				for(i=0 ; i<obj.size() ; i++){		
					if(obj.get(i).type==_FARMER && obj.get(i).state!=_MOVEMAKE && obj.get(i).team==who){	
						ORDER ord=new ORDER(who);
						ord.set(flowtime+1,obj.get(i).number);
						ord.movemake(obj.get(i).x+rand()*600-300,obj.get(i).y+rand()*600-300,_NORMALTOWER);
						order.add(ord);
						break;
					}	
				}		
			}else{			
				int rnd=(int)(rand()*3);		

				for(i=0 ; i<obj.size() ; i++){		
					if(obj.get(i).type==_CREATOR && obj.get(i).team==who){	
						ORDER ord=new ORDER(who);
						ord.set(flowtime+1,obj.get(i).number);
						if(rnd==0)
						wwn+=ord.making(_KNIFE);
						else if(rnd==1)
						wwn+=ord.making(_BASIC);
						else if(rnd==2)
						wwn+=ord.making(_ATANK);
						order.add(ord);
					}	
				}		
			}			

			nnext=10;			
		//	npop++;			
		//	maxtan++;			
		//	tan++;			
			if(flowtime%300==0){			
				for(i=0 ; i<obj.size() ; i++){		
					if(obj.get(i).type==_FARMER && obj.get(i).team==who){	
						ORDER ord=new ORDER(who);
						ord.set(flowtime+1,obj.get(i).number);
						ord.catchs();
						order.add(ord);
						break;
					}	
				}		
			}			
			if(flowtime%2000==0)			
			{			
				wwn=0;		
				System.out.println("AA");		
				next++;		
				for(i=0 ; i<obj.size() ; i++)		
				{		
					if(obj.get(i).team==who && obj.get(i).isunit==1 && obj.get(i).type!=_FARMER){	
						OBJ rr=obj.get(i).nearestunit();
						if(rr!=null){
						ORDER ord=new ORDER(who);
						ord.set(flowtime+1,obj.get(i).number);
						ord.move(rr.x,rr.y);
						order.add(ord);
						}
					}	
				}		
			}			
		}				
	}					
	void copamgr(){					
		if(sel.size()==0){				
			copa=new COPA(null);			
			 return;			
		}				
		OBJ ss=sel.get(0);				
		copa=new COPA(ss);				
	}		
	public class COPA{		
		int flag=0;	
		int n;	
		int swit[];	
		int nswit;	
		String name[];	
		int cantype[];	
		OBJ who;	
		COPA(){}	
		COPA(OBJ who){	
			this.who=who;
			n=13;
			nswit=0;
			name=new String[n];
			cantype=new int[n];
			swit=new int[n];
			name[0]="UPGRADE"; cantype[0]=0;
			name[1]="FARMER"; cantype[1]=_CREATOR;
			name[2]="BASIC";cantype[2]=_CREATOR;
			name[3]="KNIFE";cantype[3]=_CREATOR;
			name[4]="ATANK";cantype[4]=_CREATOR;
			name[5]="CREATOR";cantype[5]=_FARMER;
			name[6]="SUPPLY";cantype[6]=_FARMER;
			name[7]="CATCH";cantype[7]=_FARMER;
			name[8]="NORMALTOWER";cantype[8]=_FARMER;
			name[9]="MOVEMODE";cantype[9]=0;
			name[10]="ATTACKMODE";cantype[10]=0;
			name[11]="DESTROY";cantype[11]=0;
			name[12]="STMAN";cantype[12]=_SUPPLY;

			if(who==null)return;
			int i;
			for(i=0 ; i<n ; i++){
			if(who.type!=cantype[i] && cantype[i]!=0) continue;	
				swit[nswit]=i;
				nswit++;
			}	
		}		
		void render(Graphics2D g2){		
			g2.setColor(Color.gray);	
			g2.fillRect(0,0,100,600);	
			if(who==null)return;	
			int i;	
			for(i=0 ; i<nswit ; i++){	
			g2.setColor(new Color(0,0,255,150));	
			g2.fillRect(10,150+i*50,40,40);	
			g2.setColor(new Color(255,255,255));	
			g2.drawString(name[swit[i]],10,170+i*50);	
			}	
		}			
		void leftclick(int x,int y){			
			if(who==null)return;		
			flag=0;		
			int i;		
			int xx=x+defx,yy=y+defy;		
			for(i=0 ; i<nswit ; i++){		
			if(x>10 && x<50 && y>150+50*i && y<190+50*i){		
				flag=swit[i]+1;	
				for(int j=0 ; j<sel.size() ; j++){	
				if(flag==1){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.upgrade();
					order.add(ord);
				}	
				if(flag==2){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_FARMER);
					order.add(ord);
				}	
				if(flag==3){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_BASIC);
					order.add(ord);
				}	
				if(flag==4){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_KNIFE);
					order.add(ord);
				}	
				if(flag==5){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_ATANK);
					order.add(ord);
				}	
				if(flag==6){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_CREATOR);
					order.add(ord);
				}	
				else if(flag==7){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_SUPPLY);
					order.add(ord);
				}	
				else if(flag==8){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.catchs();
					order.add(ord);
				}	
				else if(flag==9){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_NORMALTOWER);
					order.add(ord);
				}	
				else if(flag==10){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.setstate(_MOVE);
					order.add(ord);
				}	
				else if(flag==11){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.setstate(_ATTACK);
					order.add(ord);
				}	
				else if(flag==12){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.destroyme();
					order.add(ord);
				}	
				else if(flag==13){	
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(j).number);
					ord.making(_STMAN);
					order.add(ord);
				}	
				}	
			}		
			}		
		}			
		void rightclick(int x,int y){
		}
	}	
	public class OBJ{	
		int level=1;
		int state=0;
		String name;
		OBJ lasthit;
		int number=0;
		double E;
		double A;
		double D;
		VECTOR rot;
		int type=0;
		int isunit;
		int team;
		double desx,desy;	
		double ldist=0,movedist=0;	
		double spread;	
		double x,y;	
		double vx,vy,ax,ay;	
		int delaymove;	
		int delayattack;	
		int _dm;	
		int _da;	
		public void cal(){}	
		public void vcal(){}	
		void upgrade(){}	
		void move(double x,double y){}	
		public int making(int num){return 0;}	
		void fills(Graphics2D g2,int xx[],int yy[],int n1,int n2,int n3,Color color){	
			g2.setColor(color);
			Polygon poly=new Polygon();
			poly.addPoint((int)(xx[n1]+x-defx),(int)(yy[n1]+y-defy));
			poly.addPoint((int)(xx[n2]+x-defx),(int)(yy[n2]+y-defy));
			poly.addPoint((int)(xx[n3]+x-defx),(int)(yy[n3]+y-defy));
			g2.fillPolygon(poly);
		}	
		void fills(Graphics2D g2,int xx[],int yy[],int n1,int n2,int n3,int n4,Color color){	
			g2.setColor(color);
			Polygon poly=new Polygon();
			poly.addPoint((int)(xx[n1]+x-defx),(int)(yy[n1]+y-defy));
			poly.addPoint((int)(xx[n2]+x-defx),(int)(yy[n2]+y-defy));
			poly.addPoint((int)(xx[n3]+x-defx),(int)(yy[n3]+y-defy));
			poly.addPoint((int)(xx[n4]+x-defx),(int)(yy[n4]+y-defy));
			g2.fillPolygon(poly);
		}	
		void setstate(int state){	
			this.state=state;		
		}			
		void movemake(double x,double y,int what){}			
		void destroyme(){			
			int i;		
			if(type!=_FARMER)		
			System.out.println("PL:"+team+" na:"+name+" DIE");		
			for(i=0 ; i<obj.size() ; i++)		
				if(obj.get(i)==this)	
					obj.remove(i);
			for(i=0 ; i<sel.size() ; i++)		
				if(sel.get(i)==this)	
					sel.remove(i);
			player[team].npop+=getnpop(type);		
		}			
		void autoAI(){			
			OBJ rr=nearestunit();		
			if(rr!=null)		
			{		
				if((x-rr.x)*(x-rr.x)+(y-rr.y)*(y-rr.y)<ldist*ldist)	
				{	
					maketan(rr);
				}	
				if((x-rr.x)*(x-rr.x)+(y-rr.y)*(y-rr.y)<movedist*movedist)	
				{	
					setdes(rr.x,rr.y);
				}	
			}		
		}			
		void setdes(double x,double y){}			
		void maketan(OBJ target){}			
		int ismapxy(double x,double y){			
			return map[(int)(x/sz)][(int)(y/sz)];		
		}			
		OBJ isobjxy(double x,double y){			
			int i;		
			for(i=0 ; i<obj.size() ; i++){		
				if(obj.get(i).isunit==1 && obj.get(i)!=this){	
					if((int)(obj.get(i).x/sz)==(int)(x/sz) && (int)(obj.get(i).y/sz)==(int)(y/sz))
					return obj.get(i);
				}	
			}		
			return null;		
		}			
		OBJ nearestunit(){			
			int i;		
			double min=99999999;		
			OBJ rr=null;		
			for(i=0 ; i<obj.size() ; i++)			
				if(obj.get(i)!=this && obj.get(i).team!=team && obj.get(i).isunit>=1)		
				{		
					double dist=(obj.get(i).x-x)*(obj.get(i).x-x)+(obj.get(i).y-y)*(obj.get(i).y-y);	
					if(min>dist)	
					{	
						min=dist;
						rr=obj.get(i);
					}	
				}		
			return rr;			
		}				

		OBJ allnearestunit(){				
			int i;			
			double min=99999999;			
			OBJ rr=null;			
			for(i=0 ; i<obj.size() ; i++)			
				if(obj.get(i)!=this && obj.get(i).isunit>=1)		
				{		
					double dist=(obj.get(i).x-x)*(obj.get(i).x-x)+(obj.get(i).y-y)*(obj.get(i).y-y);	
					if(min>dist)	
					{	
						min=dist;
						rr=obj.get(i);
					}	
				}		
			return rr;			
		}				
		void render(Graphics2D g2){}				
	}					
	public class TAN extends OBJ{					
		OBJ from;	
		OBJ target;	
		TAN(OBJ from,OBJ target){	
			isunit=0;
			this.from=from;
			this.target=target;
			x=from.x;
			y=from.y;
			A=30;
			ldist=from.ldist;
			spread=from.spread;
			team=from.team;
			if(target!=null)
			target.lasthit=from;
			name="TAN";
		}	
		public void vcal(){	
			double ddx,ddy;	
			double dirx=target.x-x;	
			double diry=target.y-y;	
			ddx=dirx/Math.sqrt(dirx*dirx+diry*diry);	
			ddy=diry/Math.sqrt(dirx*dirx+diry*diry);	
			vx=ddx*10;	
			vy=ddy*10;	
		}		
		public void cal(){		
			int i;	
			x+=vx;y+=vy;	
			ldist-=Math.sqrt(vx*vx+vy*vy);	
			if(ldist<0){	
				target.E-=A;
				destroyme();
				return;
			}			

					if((x-target.x)*(x-target.x)+(y-target.y)*(y-target.y)<spread*spread){	
						target.E-=A;
						destroyme();
					}	
		}				
		void render(Graphics2D g2){				
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;			
			g2.setColor(new Color(255,10,10));			
			g2.fillRect((int)x-2-defx,(int)y-2-defy,5,5);			
		}				
	}					

	public class STUN extends TAN{					
		int life;				
		STUN(OBJ from,OBJ target){				
			super(from,target);			
			life=1;	
			ldist=0;		
		}	
		public void vcal(){	
		}		
		public void cal(){		
			int i;	
			
				ldist+=10;				
				if(ldist<170){			
				}				
				else{				
					for(i=0 ; i<obj.size() ; i++){			
						if(obj.get(i).isunit>=1 && obj.get(i).team!=from.team){		
							if((x-obj.get(i).x)*(x-obj.get(i).x)+(y-obj.get(i).y)*(y-obj.get(i).y)<ldist*ldist){	
								obj.get(i).delaymove+=300;
								obj.get(i).delayattack+=300;
							}	
						}		
					}	
					destroyme();
				}
							

		}		
		void render(Graphics2D g2){	
				g2.setColor(new Color(255,255,255,30));
				Ellipse2D.Float eli=new Ellipse2D.Float();
				eli.setFrame((int)(x-defx-ldist/2),(int)(y-defy-ldist/2),ldist,ldist);
				g2.fill(eli);
		}		
	}			
	public class LASER extends TAN{			
		LASER(OBJ from,OBJ target){		
			super(from,target);	
		}		
		public void cal(){		
			int i;	
			ldist-=10;	
			if(ldist<0)	
				destroyme();

			target.E-=A;	

		}	
		void render(Graphics2D g2){	
			g2.setColor(new Color(255,255,255,30));
			g2.drawLine((int)(target.x-defx),(int)(target.y-defy),(int)(from.x-defx),(int)(from.y-defy));
		}	
	}		

	public class HETAN extends TAN{		
		int flag=0;	
		HETAN(OBJ from,OBJ target){	
			super(from,target);
		}	
		public void vcal(){	
			double ddx,ddy;
			double dirx=target.x-x;
			double diry=target.y-y;	
			ddx=dirx/Math.sqrt(dirx*dirx+diry*diry);	
			ddy=diry/Math.sqrt(dirx*dirx+diry*diry);	
			vx=ddx*10;	
			vy=ddy*10;	
		}		
		public void cal(){		
			int i;	

			if(flag==0){	
			x+=vx;y+=vy;	
			ldist-=Math.sqrt(vx*vx+vy*vy);	
			if(ldist<0){	
				flag=1;
				ldist=0;
			}	
			}					
			else {					
				ldist+=10;				
				if(ldist<150){				
					for(i=0 ; i<obj.size() ; i++){			
						if(obj.get(i).isunit>=1){		
							if((target.x-obj.get(i).x)*(target.x-obj.get(i).x)+(target.y-obj.get(i).y)*(target.y-obj.get(i).y)<ldist*ldist){	
								obj.get(i).E-=5;
							}	
						}		
					}			
				}				
				else				
					destroyme();			
			}					

		}		
		void render(Graphics2D g2){		
			if(flag==0){	
			g2.setColor(new Color(255,255,0));	
			g2.fillRect((int)x-2-defx,(int)y-2-defy,5,5);	
			}	
			else if(flag==1){	
				g2.setColor(new Color(255,255,255,30));
				Ellipse2D.Float eli=new Ellipse2D.Float();
				eli.setFrame((int)(target.x-defx-ldist/2),(int)(target.y-defy-ldist/2),ldist,ldist);
				g2.fill(eli);
			}	
		}		
	}			
	public class UNIT extends OBJ{			
		double befx,befy;		
		double maxv;	
		double maxa;	
		int dx[]={1,-1,0,0,0,1,1,-1,-1};	
		int dy[]={0,0,1,-1,0,1,-1,1,-1};	
		double goquex[];	
		double goquey[];	
		int gorear;	
		int gofront;	

		int whatmake;	

		UNIT(double x,double y,int team)	
		{	
			state=_ATTACK;
			int i, j;
			spread=10;
			delaymove=0;
			delayattack=0;
			isunit=1;
			int gofront=0;
			int gorear=0;
			goquex=new double[10000];
			goquey=new double[10000];
			this.x=x;
			this.y=y;
			desx=x;desy=y;
			befx=x;befy=y;
			this.team=team;
			vx=vy=0;
			rot=new VECTOR(0,-1,0);
			number=realnumber;
			realnumber++;
		}	
		void move(double x,double y){	
		//	state=_ATTACK;
			setdes(x,y);
		}	
		void movemake(double x,double y,int what){	
			state=_MOVEMAKE;
			whatmake=what;
			setdes(x,y);
		}	
		void setdes(double x,double y){	
			desx=x;
			desy=y;
			int front,rear, nx, ny, end, lmt;
			front=0;rear=1;
			int quex[]=new int[10000];
			int quey[]=new int[10000];		
			int formap[][]=new int[100][100];		
			int i,j;		
			end=0;		
			quex[1]=(int)(this.x/sz);		
			quey[1]=(int)(this.y/sz);		
			for(i=0 ; i<100 ; i++)		
				for(j=0 ; j<100 ; j++)	
					formap[i][j]=-1;
			do{		
				if(rear>9000) return;	
				front++;	
				for(i=0 ; i<4 ; i++)	
				{	
					nx=quex[front]+dx[i];
					ny=quey[front]+dy[i];
					if(map[nx][ny]>=0 && formap[nx][ny]==-1 || (int)(desx/sz)==nx && (int)(desy/sz)==ny){		
						rear++;	
						quex[rear]=nx;	
						quey[rear]=ny;	
						formap[nx][ny]=i;	
						if((int)(desx/sz)==nx && (int)(desy/sz)==ny){	
							end=1;
						}	
					}		
				}			
			}while(rear!=front && end==0);				
			if(end==0) return;				
			//System.out.println("rearfront : "+rear+" "+front+" "+end);				
			nx=(int)(desx/sz);ny=(int)(desy/sz);				
			//System.out.println("des : "+(int)(this.x/sz)+" "+(int)(this.y/sz));				
			lmt=3000;				
			int bef=-1;		
			gofront=0;		
			gorear=0;		
			gorear++;		
			goquex[gorear]=x;		
			goquey[gorear]=y;		
			while(!(nx==(int)(this.x/sz) && ny==(int)(this.y/sz)) && lmt>0){		
				lmt--;	
				int nnx=nx-dx[formap[nx][ny]];	
				int nny=ny-dy[formap[nx][ny]];	
				try{	
				}catch(Exception ex){}	
				if(formap[nx][ny]!=bef){	
					gorear++;
					goquex[gorear]=nx*sz+sz/2;
					goquey[gorear]=ny*sz+sz/2;
					bef=formap[nx][ny];
				}	
				nx=nnx;ny=nny;	
			}		
		//	gorear--;		
		}			
		public void vcal()			
		{			
			double dirx,diry,ddx,ddy;		
			ddx=0;		
			ddy=0;		
			if(gofront<gorear)//stack		
				if((goquex[gorear]-x)*(goquex[gorear]-x)+(goquey[gorear]-y)*(goquey[gorear]-y)<900)	
					gorear--;
			if(gofront<gorear)		
			{		
				dirx=goquex[gorear]-x;	
				diry=goquey[gorear]-y;	
				double ddd=Math.sqrt(dirx*dirx+diry*diry);	
				if(ddd!=0){	
					ddx=dirx/ddd;
					ddy=diry/ddd;
				}	
			}		
			OBJ rr=allnearestunit();		
			if(rr!=null){		
				double ddd=Math.sqrt((rr.x-x)*(rr.x-x)+(rr.y-y)*(rr.y-y));	
				if(ddd<20 && ddd!=0){	
					dirx=x-rr.x;
					diry=y-rr.y;
					ddx=dirx/ddd;
					ddy=diry/ddd;
				}
			}	
	//	if(VECTOR.outer(new VECTOR(ddx,ddy,0),rot).z>0)		
	//		rightturn();	
	//	else		
	//		leftturn();	
	//		double ttt=Math.sqrt(ddx*ddx+ddy*ddy);	
	//		ddx=rot.x*ttt;	
	//		ddy=rot.y*ttt;	
			ax=ddx*maxa;	
			ay=ddy*maxa;	
		}		
		public void leftturn(){		
			rot=VECTOR.rotate(rot,new VECTOR(0,0,0.4));	
		}		
		public void rightturn(){		
			rot=VECTOR.rotate(rot,new VECTOR(0,0,-0.4));	
		}		
		public void cal()		
		{		
			vx*=0.9;	
			vy*=0.9;	
			vx+=ax;vy+=ay;	
			double vv=Math.sqrt(vx*vx+vy*vy);	
			if(vv>maxv){	
				vx=maxv*vx/vv;
				vy=maxv*vy/vv;
			}	
			if(delaymove>0)	
				vx=vy=0;


			if(E<=0)destroyme();	

			delayattack--;	
			delaymove--;	
			if(delayattack<0)	
				delayattack=0;
			if(delaymove<0)	
				delaymove=0;
			x+=vx;	
			if(ismapxy(x,y)<=-1){	
				x=befx;
				vx=0;
			}	
			y+=vy;	
			if(ismapxy(x,y)<=-1){	
				y=befy;
				vy=0;
			}	
			befx=x;befy=y;	
			if(state==_ATTACK)	
				autoAI();
			if(gofront>=gorear){	
				making(whatmake);
			}	
			if(ismapxy(x,y)==-1){y++;befy++;}	
		}		
		void render(Graphics2D g2){}		
	}			

	public class SUPPLY extends BUILD{			
		SUPPLY(double x,double y,int team){		
			super(x,y,team);	
			name="SUPPLY";
			E=500;
			int i,j;
			player[team].maxtan+=100;
			type=_SUPPLY;
		}		
		public int making(int num){		
			if(num==_STMAN && level>=2){	
			if(unitmake(_STMAN,x+rand()*5,y+30+rand()*5,team)==null)return 0;	
			}	
			return 1;	
		}	
		void render(Graphics2D g2){	
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;
			int xx[]={-8,8,15,-15,-7,0,7,0,3,0,0,-3};
			int yy[]={11,11,3,3,-11,-3,-11,-7,-11,-19,-25,-11};
		//	g2.fillRect((int)x-defx,(int)y-defy,10,10);
		fills(g2,xx,yy,9,6,5,4,ctable[team]);	
			fills(g2,xx,yy,10,11,7,new Color(55,0,0));
			fills(g2,xx,yy,7,8,10,new Color(5,0,0));
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));	
			}	
		}		
		void upgrade(){		
			if(player[team].tan>=200){	
				player[team].tan-=200;
				level++;
				player[team].maxtan+=50;
			}	
		}		
		void destroyme(){		
			player[team].maxtan-=100;	
			super.destroyme();	
		}		
		public void cal(){		
			if(flowtime%100==0)	
			player[team].tan+=level;
			if(player[team].tan>player[team].maxtan) player[team].tan=player[team].maxtan;
			//player[team].money++;
			super.cal();
		}	
	}		
	public class CREATOR extends BUILD{		
		int areas;	
		CREATOR(double x,double y,int team){	
			super(x,y,team);
			name="CREATOR";
			E=500;
			A=10;
			D=2;
			int i,j;
			type=_CREATOR;
		}		
		public int making(int num){		
			if(num==_FARMER){	
			if(unitmake(_FARMER,x+rand()*5,y+30+rand()*5,team)==null)return 0;	
			}else if(num==_BASIC){	
			if(unitmake(_BASIC,x+rand()*5,y+30+rand()*5,team)==null)return 0;	
			}else if(num==_KNIFE){	
			if(unitmake(_KNIFE,x+rand()*5,y+30+rand()*5,team)==null)return 0;	
			}else if(num==_ATANK && level>=2){	
			if(unitmake(_ATANK,x+rand()*5,y+30+rand()*5,team)==null)return 0;	
			}	
			return 1;	
		}		
		void upgrade(){		
			if(player[team].tan>=200){	
				player[team].tan-=200;
				level++;
			}	
		}		
		void maketan(OBJ target){		
			if(delayattack>0)	
				return;
			obj.add(new TAN(this,target));	
			delayattack=_da;	
			delaymove=_dm;	
		}		
		void render(Graphics2D g2){		
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;	
			int xx[]={-8,8,15,-15,-7,0,7,0,3,0,0,-3};	
			int yy[]={11,11,3,3,-11,-3,-11,-7,-11,-19,-25,-11};	
		//	g2.fillRect((int)x-defx,(int)y-defy,10,10);	
		fills(g2,xx,yy,9,6,5,4,ctable[team]);		
			fills(g2,xx,yy,4,3,0,5,new Color(205,0,0));
			fills(g2,xx,yy,0,1,5,new Color(155,0,0));
			fills(g2,xx,yy,5,1,2,6,new Color(105,0,0));
			fills(g2,xx,yy,10,11,7,new Color(55,0,0));
			fills(g2,xx,yy,7,8,10,new Color(5,0,0));
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));
			}
		}	
	}		
	public class FARMER extends UNIT{		
		int mon;	
		int flag;	
		OBJ cre;	
		FARMER(double x,double y,int team){	
			super(x,y,team);
			type=_FARMER;
			name="FARMER";
			E=100;
			maxv=2.5;
			_dm=10;
			_da=50;
			maxa=0.5;
			mon=0;
			flag=0;
			cre=null;

		}	

		OBJ nearestcreator(){	
			int i;
			double min=99999999;			
			OBJ rr=null;			
			for(i=0 ; i<obj.size() ; i++)			
				if(obj.get(i)!=this && obj.get(i).team==team && obj.get(i).name.equals("CREATOR"))		
				{		
					double dist=(obj.get(i).x-x)*(obj.get(i).x-x)+(obj.get(i).y-y)*(obj.get(i).y-y);	
					if(min>dist)	
					{	
						min=dist;
						rr=obj.get(i);
					}	
				}		
			return rr;			
		}				
		void upgrade(){				
			if(player[team].tan>=70){			
				player[team].tan-=70;
				level++;
			}	
		}		
		void move(double x,double y){		
			flag=6;	
			super.move(x,y);	
		}		
		void movemake(double x,double y,int what){		
			flag=6;	
			super.movemake(x,y,what);	
		}		
		public void cal(){		
			int getting=50+level*50;	
			int i, j, k;	
			if(flag==0){//find	
				double dd=99999999;			
				int nx,ny;			
				nx=0;ny=0;			
				for(j=0 ; j<100 ; j++)			
					for(k=0 ; k<100 ; k++)		
						if(map[j][k]>1 && area[j][k]==0)	
						if((j*sz-x)*(j*sz-x)+(k*sz-y)*(k*sz-y)<dd)	
						{	
							dd=(j*sz-x)*(j*sz-x)+(k*sz-y)*(k*sz-y);
							nx=j;ny=k;
						}	
				if(dd<99999999){			
				area[nx][ny]=1;			
				nx*=sz;ny*=sz;			
				setdes(nx+sz/2,ny+sz/2);			
				flag=1;			
				}		
			}			
			else if(flag==1){			
				if(mon>=getting){		
					flag=2;	
				}		
				else{		
					if(getmapxy(x,y)>1){	
						map[(int)(x/sz)][(int)(y/sz)]--;
						area[(int)(x/sz)][(int)(y/sz)]=1;
						mon++;vx=vy=0;
					}	
					else{	
						flag=0;
					}	
				}		
			} else if(flag==2){		
				cre=nearestcreator();	
				setdes(cre.x,cre.y);	
				flag=3;	
			} else if(flag==3){		
				double dd=(cre.x-x)*(cre.x-x)+(cre.y-y)*(cre.y-y);	
				if(dd<2000)	
				{	
					player[team].money+=mon;
					mon=0;flag=0;
				}	
				else flag=2;	
			}		
			super.cal();		
		}			
		public int making(int num){			
			if(num==_CREATOR){
			if(buildmake(_CREATOR,x,y,team)!=null){
			destroyme();
			return 1;
			}
			}else if(num==_SUPPLY){
			if(buildmake(_SUPPLY,x,y,team)!=null){
			destroyme();
			return 1;
			}}else if(num==_NORMALTOWER){
			if(buildmake(_NORMALTOWER,x,y,team)!=null){
			destroyme();
			return 1;
			}
			}
			return 0;
		}	
		void render(Graphics2D g2){	
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;
			int xx[]={0,0,-10,10};
			int yy[]={0,-10,5,5};
	//		g2.fillRect((int)x-defx,(int)y-defy,5,5);
			fills(g2,xx,yy,1,0,2,ctable[team]);
			fills(g2,xx,yy,1,3,0,new Color(150,0,0));
			fills(g2,xx,yy,0,2,3,new Color(50,0,0));
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));
			}

		}	
	}		

	public class KNIFE extends UNIT{		
		KNIFE(double x,double y,int team){	
			super(x,y,team);
			name="KNIFE";
			E=100;
			A=0.7;
			D=5;
			maxv=3;
			ldist=35;
			movedist=200;
			_dm=10;
			_da=50;
			maxa=0.5;
			type=_KNIFE;
		}	
		void maketan(OBJ target){		
			if(delayattack>0)	
				return;
			//obj.add(new TAN(this,target));	
			target.E-=30;	
			delayattack=_da;	
			delaymove=_dm;	
		}		
		void render(Graphics2D g2){		
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;	
			int xx[]={0,-3,-8,-8,8,8,0,-3,3,-2,2,-2,2,3};	
			int yy[]={0,0,0,3,3,0,-15,-12,-12,3,3,8,8,0};	
		//	g2.setColor(Color.white);	
		//	for(int i=0 ; i<=gorear ; i++)	
		//	g2.fillRect((int)goquex[i]-defx,(int)goquey[i]-defy,5,5);	
			fills(g2,xx,yy,6,7,1,0,new Color(150,150,150));	
			fills(g2,xx,yy,6,0,13,8,new Color(200,200,200));
			fills(g2,xx,yy,2,5,4,3,ctable[team]);
			fills(g2,xx,yy,9,10,12,11,ctable[team]);
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));
			}

		}	
	}		

	public class ATANK extends UNIT{		
		ATANK(double x,double y,int team){	
			super(x,y,team);
			name="ATANK";
			E=150;
			maxv=2;	
			ldist=200;	
			movedist=300;	
			_dm=205;	
			_da=200;	
			maxa=0.3;	
			type=_ATANK;	
		}		
		void maketan(OBJ target){		
			if(delayattack>0 || player[team].tan<20)	
				return;
			obj.add(new HETAN(this,target));	
			player[team].tan-=20;	
			delayattack=_da;	
			delaymove=_dm;	
		}		
		void render(Graphics2D g2){	
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;
			int xx[]={3,-3,3,-9,-3,3,9,-9,-3,3,9,-3};
			int yy[]={9,-9,-9,-3,-3,-3,-3,3,3,3,3,9};
			fills(g2,xx,yy,4,5,9,8,ctable[team]);
			fills(g2,xx,yy,1,3,4,new Color(150,150,150));
			fills(g2,xx,yy,2,5,6,new Color(150,150,150));
			fills(g2,xx,yy,7,8,11,new Color(150,150,150));
			fills(g2,xx,yy,0,9,10,new Color(150,150,150));
			fills(g2,xx,yy,1,2,4,5,new Color(50,50,50));
			fills(g2,xx,yy,3,4,8,7,new Color(50,50,50));
			fills(g2,xx,yy,8,9,0,11,new Color(50,50,50));
			fills(g2,xx,yy,5,6,10,9,new Color(50,50,50));
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));
			}
		}	
	}		
	public class BASIC extends UNIT{		
		BASIC(double x,double y,int team){	
			super(x,y,team);
			name="BASIC";
			E=100;
			A=0.7;
			D=5;
			maxv=3;
			ldist=80;
			movedist=200;
			_dm=55;
			_da=50;
			maxa=0.5;
			type=_BASIC;	
		}		
		void maketan(OBJ target){		
			if(delayattack>0 || player[team].tan<2)	
				return;
			obj.add(new TAN(this,target));	
			player[team].tan-=2;	
			delayattack=_da;	
			delaymove=_dm;	
		}		
		void render(Graphics2D g2){		
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;	
			int xx[]={3,-3,3,-9,-3,3,9,-9,-3,3,9,-3};	
			int yy[]={9,-9,-9,-3,-3,-3,-3,3,3,3,3,9};	
			fills(g2,xx,yy,4,5,9,8,ctable[team]);	
			fills(g2,xx,yy,1,3,4,new Color(150,150,150));	
			fills(g2,xx,yy,2,5,6,new Color(150,150,150));
			fills(g2,xx,yy,7,8,11,new Color(150,150,150));
			fills(g2,xx,yy,0,9,10,new Color(150,150,150));
			fills(g2,xx,yy,1,2,4,5,new Color(50,50,50));
			fills(g2,xx,yy,3,4,8,7,new Color(50,50,50));
			fills(g2,xx,yy,8,9,0,11,new Color(50,50,50));
			fills(g2,xx,yy,5,6,10,9,new Color(50,50,50));
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));
			}
		}	
	}		

	public class STMAN extends UNIT{		
		STMAN(double x,double y,int team){	
			super(x,y,team);
			name="STMAN";
			E=100;
			maxv=3;
			ldist=0;
			movedist=0;
			_dm=55;
			_da=50;
			maxa=0.5;
			type=_STMAN;	
		}		
		void destroyme(){
			obj.add(new STUN(this,null));
			super.destroyme();
		}	
		void render(Graphics2D g2){		
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;	
			int xx[]={3,-3,3,-9,-3,3,9,-9,-3,3,9,-3};	
			int yy[]={9,-9,-9,-3,-3,-3,-3,3,3,3,3,9};	
			fills(g2,xx,yy,4,5,9,8,ctable[team]);	
			fills(g2,xx,yy,1,3,4,new Color(150,150,150));	
			fills(g2,xx,yy,2,5,6,new Color(150,150,150));
			fills(g2,xx,yy,7,8,11,new Color(150,150,150));
			fills(g2,xx,yy,0,9,10,new Color(150,150,150));
			fills(g2,xx,yy,1,2,4,5,new Color(50,50,50));
			fills(g2,xx,yy,3,4,8,7,new Color(50,50,50));
			fills(g2,xx,yy,8,9,0,11,new Color(50,50,50));
			fills(g2,xx,yy,5,6,10,9,new Color(50,50,50));
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));
			}
		}	
	}		

	public class NORMALTOWER extends TOWER{		
		NORMALTOWER(double x,double y,int team){	
			super(x,y,team);	
			E=300;	
			A=30;	
			D=2;	
			ldist=200;	
			_dm=0;	
			_da=50;	
			int i,j;	
			type=_NORMALTOWER;	
		}		
		void maketan(OBJ target){		
			if(delayattack>0 || player[team].tan<2)	
				return;
			obj.add(new TAN(this,target));	
			player[team].tan-=2;	
			delayattack=_da;	
			delaymove=_dm;
		}	
		void render(Graphics2D g2){	
			if(x-defx<0 || x-defx>800 || y-defy<0 || y-defy>600) return;
			int xx[]={0,-5,-8,-8,0,8,8,0,5,0,0,0,-5,5,0,-7,0,7};
			int yy[]={0,-2,-2,3,8,3,-2,3,-2,-7,-10,-13,-15,-15,-17,-15,-23,-15};
		//	g2.fillRect((int)x-defx,(int)y-defy,15,15);
			fills(g2,xx,yy,2,3,4,7,ctable[team]);
			fills(g2,xx,yy,4,5,6,7,new Color(205,0,0));
			fills(g2,xx,yy,2,7,6,9,new Color(155,0,0));
			fills(g2,xx,yy,1,0,11,12,new Color(105,0,0));
			fills(g2,xx,yy,0,8,13,11,new Color(55,0,0));
			fills(g2,xx,yy,10,17,14,new Color(0,5,0,0));
			fills(g2,xx,yy,10,15,14,new Color(0,105,0));
			fills(g2,xx,yy,14,15,16,new Color(0,55,0));
			fills(g2,xx,yy,14,16,17,new Color(0,5,0));
			if(level>=2){
			g2.setColor(Color.white);
			g2.drawString("Lv"+level,(int)(x-defx),(int)(y-defy));
			}
		}	
	}		
	class TOWER extends BUILD{		
		TOWER(double x,double y,int team){	
			super(x,y,team);
			name="TOWER";
		}	
		public void cal(){	
			delayattack--;
			super.cal();
			autoAI();
		}	
	}					
	class AREA{					
		CREATOR owner;				
		int money;				
		int team;				
		int n;				
		AREA nearestArea(){				
			AREA who=null;			
			int i;			
			double min=99999999;			
			for(i=0 ; i<100 ; i++){			
				if(are[i]!=this && are[i].team!=team && are[i].owner!=null && owner!=null){		
					double dd=(are[i].owner.x-owner.x)*(are[i].owner.x-owner.x)+(are[i].owner.y-owner.y)*(are[i].owner.y-owner.y);	
					if(min>dd)	
					{	
						min=dd; who=are[i];
					}
				}	
			}		
			return who;		
		}			
		void cal(){			
			n=0;		
			if(owner==null)return;		
			team=owner.team;		
			int i;		
			for(i=0 ; i<obj.size() ; i++)		
			{		
				if(are[getareaxy(obj.get(i).x,obj.get(i).y)]==this)	
					n++;
			}		
		}			
	}		
	class BUILD extends OBJ{		
		BUILD(double x,double y,int team){	
			isunit=2;
			map[(int)(x/sz)][(int)(y/sz)]=-2;
			x=(int)(x/sz)*sz+sz/2;
			y=(int)(y/sz)*sz+sz/2;
			this.x=x;
			this.y=y;
			this.team=team;

			number=realnumber;
			realnumber++;
		}	
		public void cal()	
		{	
			if(E<=0){destroyme();
			System.out.println("CALBUILD");}
		}	
		void destroyme(){	
			super.destroyme();
			map[(int)(x/sz)][(int)(y/sz)]=0;
			System.out.println("DESTROYBUILD");
		}	
	}		
	GREGRO(String args[]){		
		if(args[0].equals("S")==true)	
			listen();
		if(args[0].equals("C")==true)	
			connect(args[1]);
		ctable[0]=new Color(255,0,0);	
		ctable[1]=new Color(255,0,0);	
		ctable[2]=new Color(0,255,0);	
		ctable[3]=new Color(0,0,255);	
		ctable[4]=new Color(150,150,0);	
		ctable[5]=new Color(0,150,150);	
		ctable[6]=new Color(150,0,150);	
		ctable[7]=new Color(100,100,100);	
		ctable[8]=new Color(100,100,100);	
		ctable[9]=new Color(100,100,100);	
int k, i, j;			
		are=new AREA[100];	
		for(i=0 ; i<100 ; i++)	
			are[i]=new AREA();
		mpr=0;	
		flowtime=0;	
		defx=defy=0;	
		mode='S';	
		sz=30;	
		copa=new COPA(null);	
		map=new int[100][100];	
		area=new int[100][100];	
		keyboard=new int[256];	
		obj=new Vector <OBJ> ();	
		sel=new Vector <OBJ> ();	
		order=new Vector <ORDER> ();	
		beforder=new Vector <ORDER> ();	
		for(i=1 ; i<100 ; i++)	
			player[i]=new TEAM(i);
		if(args[0].equals("L")==true)	
			player[3-ME].money=Integer.parseInt(args[1]);
		for(i=0 ; i<256 ; i++) keyboard[i]=0;	
		for(i=0 ; i<100 ; i++)	
			for(j=0 ; j<100 ; j++){
				map[i][j]=0;	
				if(i*j==0 || i==99 || j==99)	
					map[i][j]=-1;
			//	if((int)(rand()*5)==0)	
				 map[i][j]=-1;	
				area[i][j]=0;	
			}		

		for(k=0 ; k<64 ; k++){			
			int sx,sy;		
			sx=(k/8)*10+1;		
			sy=(k%8)*10+1;		
			for(i=sx ; i<sx+10 ; i++)		
				for(j=sy ; j<sy+10 ; j++)	
				{	
					if(map[i][j]>=0)
					area[i][j]=k+1;
				}	
		}			
		for(i=10 ; i<30 ; i++)			
			for(j=10 ; j<30 ; j++)		
			{		
				map[i][j]=0;	
			}		
		for(i=10 ; i<30 ; i++)			
			for(j=40 ; j<60 ; j++)		
			{		
				map[i][j]=0;	
			}		
		for(i=10 ; i<30 ; i++)			
			for(j=70 ; j<90 ; j++)		
			{		
				map[i][j]=0;
			}	

		for(i=40 ; i<60 ; i++)		
			for(j=10 ; j<30 ; j++)	
			{	
				map[i][j]=0;
			}	
		for(i=40 ; i<60 ; i++)		
			for(j=40 ; j<60 ; j++)	
			{	
				map[i][j]=0;
			}	
		for(i=40 ; i<60 ; i++)		
			for(j=70 ; j<90 ; j++)	
			{	
				map[i][j]=0;
			}	

		for(i=70 ; i<90 ; i++)		
			for(j=10 ; j<30 ; j++)	
			{	
				map[i][j]=0;
			}	
		for(i=70 ; i<90 ; i++)		
			for(j=40 ; j<60 ; j++)	
			{	
				map[i][j]=0;
			}	
		for(i=70 ; i<90 ; i++)		
			for(j=70 ; j<90 ; j++)	
			{	
				map[i][j]=0;
			}	
		for(i=20 ; i<22 ; i++)		
			for(j=10 ; j<90 ; j++)	
			{	
				map[i][j]=0;
				map[j][i]=0;
			}	
		for(i=50 ; i<52 ; i++)		
			for(j=10 ; j<90 ; j++)	
			{	
				map[i][j]=0;
				map[j][i]=0;
			}	
		for(i=80 ; i<82 ; i++)		
			for(j=10 ; j<90 ; j++)	
			{	
				map[i][j]=0;
				map[j][i]=0;
			}	
		for(i=10 ; i<20 ; i++)		
			for(j=10 ; j<90 ; j++)	
			{	
				if(map[i][j]==0)
				map[i][j]=350;
			}	
		for(i=40 ; i<50 ; i++)		
			for(j=10 ; j<90 ; j++)	
			{	
				if(map[i][j]==0)
				map[i][j]=350;
			}	

		for(i=70 ; i<80 ; i++)		
			for(j=10 ; j<90 ; j++)	
			{	
				if(map[i][j]==0)
				map[i][j]=350;
			}	
	//	obj.get(1).setdes(1000,1000);		
		buildmake(_CREATOR,1525,1525,3);		
		buildmake(_CREATOR,625,625,1);	
		buildmake(_CREATOR,2425,2425,2);
	//	obj.get(0).setdes(500,300);		
		TIMER timer=new TIMER();		
		timer.start();		
	//	TIMER2 timer2=new TIMER2();		
	//	timer2.start();		

	}	
	public void mouseDragged(MouseEvent e){	
		mmx=e.getX()+defx;
		mmy=e.getY()-30+defy;
	}	
	public void mouseMoved(MouseEvent e){	
	}	
	public void mouseExited(MouseEvent e){	
	}	
	public void mouseClicked(MouseEvent e){	
	}	
	public void mouseEntered(MouseEvent e){	
	}	
	public void mouseReleased(MouseEvent e){	
		System.out.println("pt : "+(e.getX()+defx)+","+(e.getY()-30+defy));
		mpr=0;			
		if(e.getButton()==MouseEvent.BUTTON3)			
			System.out.println("button3");		
		int i, temp;			
		mmx=e.getX()+defx;			
		mmy=e.getY()-30+defy;			
		if(mx>mmx){			
			temp=mx;mx=mmx;mmx=temp;		
		}			
		if(my>mmy){			
			temp=my;my=mmy;mmy=temp;		
		}			
		if(e.getButton()==MouseEvent.BUTTON3){			
			for(i=0 ; i<sel.size() ; i++){		
				if(sel.get(i).isunit==1){	
					//sel.get(i).move(mmx,mmy);
					ORDER ord=new ORDER(ME);
					ord.set(flowtime+1,sel.get(i).number);
					ord.move(mmx,mmy);
					order.add(ord);
				}	
			}		
		}			
		if(mode=='S' && e.getButton()==MouseEvent.BUTTON1 && e.getX()>100){			
			//for(i=0 ; i<sel.size() ; i++)		
			while(sel.size()!=0)		
				sel.remove(0);	
			for(i=0 ; i<obj.size() ; i++)		
			{		
				if(obj.get(i).isunit>=1 && obj.get(i).team==ME){	
					if(obj.get(i).x>=mx && obj.get(i).x<=mmx && obj.get(i).y>=my && obj.get(i).y<=mmy)
					sel.add(obj.get(i));
				}
			}	
		}		
		System.out.println("Released");		
	}			
	public void mousePressed(MouseEvent e){			
		mode='S';		
		mpr=e.getButton();		
		if(MouseEvent.BUTTON1==mpr){		
		copa.leftclick(e.getX(),e.getY()-30);		
		}		
		if(MouseEvent.BUTTON3==mpr){		
		copa.rightclick(e.getX(),e.getY()-30);		
		}		
		mx=e.getX()+defx;		
		my=e.getY()-30+defy;		
	}		
	public void keyTyped(KeyEvent e){		
	}		
	public void keyPressed(KeyEvent e){		
		keyboard[e.getKeyCode()]=1;	
	}		
	public void keyReleased(KeyEvent e){		
		System.out.println(e.getKeyCode());	
		keyboard[e.getKeyCode()]=0;	
		mode=e.getKeyCode();	
	}		

	public class TIMER extends Thread{		
		public void run(){	
			int i, j, k;
			while(true)
			{		
				try{	
					if(keyboard[38]==1) defy-=30;
					if(keyboard[40]==1) defy+=30;
					if(keyboard[37]==1) defx-=30;
					if(keyboard[39]==1) defx+=30;
					//TIMER2 timer2=new TIMER2();
					//timer2.start();
					for(i=0;i<100 ; i++)for(j=0;j<100;j++)area[i][j]=0;
			k=0;		
			flowtime++;		
			//send & recev		
		//	for(i=2 ; i<4 ; i++)		
		//	player[i].computerAI();		
			if(socket==null){		
				player[2].computerAI();	
			}
			//player[3].computerAI();
			if(flowtime%5==0){	
			if(socket!=null){	
			sendorder(outs);	
			recvorder(ins);	
			}	
			for(k=1 ; k<4 ; k++)	
			for(i=0 ; i<beforder.size() ; i++){	
				if(beforder.get(i).team==k)
				beforder.get(i).excute();
			}	
			beforder=order;	
			order=new Vector<ORDER>();	
			}	
			for(i=0 ; i<obj.size() ; i++){	
				obj.get(i).vcal();
				obj.get(i).cal();
			}					

			if(flowtime%3000==0){					
				for(i=0 ; i<100 ; i++)				
					for(j=0 ; j<100 ; j++)			
						if(map[i][j]>=1){		
							map[i][j]+=200;	
							if(map[i][j]>350)	
								map[i][j]=350;
						}		
			}					
			if(flowtime%100==0){					
			int have[]=new int[100];					
			int has[]=new int[100];					
			for(i=0 ; i<100 ; i++) have[i]=0;					
			for(i=0 ; i<100 ; i++) has[i]=0;					
			for(i=0 ; i<obj.size() ; i++) if(obj.get(i).isunit==1) has[obj.get(i).team]++;		
			for(i=0 ; i<obj.size() ; i++) if(obj.get(i).isunit==2) have[obj.get(i).team]++;		

			System.out.println("Flowtime : "+flowtime);		
			for(i=0 ; i<3 ; i++){ System.out.println();System.out.print("p"+i+":"+have[i+1]+","+has[i+1]+"("+player[i+1].money+","+player[i+1].tan+ "/ ");		
				for(j=0 ; j<obj.size(); j++) if(obj.get(j).team==i+1) System.out.print((obj.get(j).name).substring(0,1));	
 System.out.println();					
			}		
			System.out.println();		
			}		
			copamgr();		
					repaint();
					Thread.sleep(20);
				}catch(Exception ex){System.out.println(ex);}	
			}		
		}			
	}				

	public void paint(Graphics g){				
		int i, j;			
		Graphics2D g2=(Graphics2D)g;			
		g2.setColor(Color.black);			
		g2.fillRect(0,0,800,600);			
		for(i=0 ; i<100 ; i++)			
		{			
			for(j=0 ; j<100 ; j++)		
			{		
				if(i*sz-defx<0 || i*sz-defx>800 || j*sz-defy<0 || j*sz-defy>600) continue;	
				if(map[i][j]==-1)	
					g2.setColor(new Color((int)(rand()*10),(int)(rand()*10),(int)(rand()*10)));
				else if(map[i][j]==0){	
					g2.setColor(new Color(0,40,0));
				}	
				else if(map[i][j]>=1) g2.setColor(new Color(0,0,map[i][j]*2/3));	
				g2.fillRect(i*sz-defx,j*sz-defy,sz,sz);	
			//	if(area[i][j]==selarea)	
			//		g2.setColor(new Color(0,100,0));
			//	g2.fillRect(i*sz-defx,j*sz-defy,sz,sz);	
			}		
		}			
		if(mode=='S'){			
			g2.setColor(new Color(255,255,255));		
			if(mpr==1){		
				int rrx,rry,rrrx,rrry;	
				if(mx<mmx){rrx=mx;rrrx=mmx;}	
				else {rrx=mmx;rrrx=mx;}	
				if(my<mmy){rry=my;rrry=mmy;}	
				else {rry=mmy;rrry=my;}	
				g2.drawRect((int)rrx-defx,(int)rry-defy,(int)(rrrx-rrx),(int)(rrry-rry));
			}	
		}		
		g2.setColor(new Color(0,200,0));		
		for(i=0 ; i<sel.size() ; i++){		
			g2.fillRect((int)sel.get(i).x-30-defx,(int)sel.get(i).y-5-defy,60,10);	
		}		
		g2.setColor(new Color(0,0,250));		
		for(i=0 ; i<obj.size() ; i++){		
			obj.get(i).render(g2);	
			if(obj.get(i).isunit>=1)	
				g2.fillRect((int)(obj.get(i).x-defx-obj.get(i).E/20),(int)(obj.get(i).y-defy+20),(int)(obj.get(i).E/10),5);
		}		
	//	System.out.println(obj.size());		

		g2.setColor(Color.white);		

		copa.render(g2);			
		for(i=0 ; i<100 ; i+=5)			
		{			
			for(j=0 ; j<100 ; j+=5)		
			{		
			//	if(i*sz-defx<0 || i*sz-defx>800 || j*sz-defy<0 || j*sz-defy>600) continue;	
				if(map[i][j]==-1)	
					g2.setColor(Color.black);
				else if(map[i][j]==0){	
					g2.setColor(new Color(0,40,0));
				}	
				else if(map[i][j]>=1) g2.setColor(new Color(0,0,map[i][j]*2/3));	
				g2.fillRect(i,j,10,10);	
			}		
		}			
		for(i=0 ; i<obj.size() ; i++){		
			obj.get(i).render(g2);	
			if(obj.get(i).isunit>=1){	
				g2.setColor(ctable[obj.get(i).team]);
				g2.fillRect((int)(obj.get(i).x/sz),(int)(obj.get(i).y/sz),1,1);
			}	
		}		
		g2.setColor(Color.white);		
		g2.drawString("Money : "+player[ME].money,100,30);		
		g2.drawString("Tan : "+player[ME].tan+"/"+player[1].maxtan,200,30);		
		g2.drawString("Pop : "+player[ME].npop,300,30);		
	}			
	public static void main(String args[]){			
		GREGRO a=new GREGRO(args);		
		JFrame frame=new JFrame();		
		frame.add(a);		
		frame.setSize(0,0);	
		frame.addKeyListener(a);	
		frame.addMouseListener(a);	
		frame.addMouseMotionListener(a);	
		frame.addWindowListener(new appcloser());	
		frame.show();	
	}		
	public static class appcloser extends WindowAdapter		
	{		
		public void windowClosing(WindowEvent e){	
			System.exit(1);
		}	
	}		
}			

class VECTOR			
{
public double x,y,z;
VECTOR(){x=0;y=0;z=0;}
VECTOR(double x,double y,double z){
this.x=x;this.y=y;this.z=z;
}
public void input(InputStream in)
{
DataInputStream dis=new DataInputStream(in);
try{
x=dis.readDouble();
y=dis.readDouble();
z=dis.readDouble();
}catch(Exception ex){}
}
public void output(OutputStream out)
{
DataOutputStream dos=new DataOutputStream(out);
try{
dos.writeDouble(x);
dos.writeDouble(y);
dos.writeDouble(z);
}catch(Exception ex){}
}
public static VECTOR inner(VECTOR A,VECTOR B){
VECTOR C=new VECTOR(A.x*B.x,A.y*B.y,A.z*B.z);
return C;
}
public static double innervalue(VECTOR A,VECTOR B){
return A.x*B.x+A.y*B.y+A.z*B.z;
}
public double value(){
return Math.sqrt(x*x+y*y+z*z);
}
public static VECTOR outer(VECTOR A,VECTOR B){
VECTOR C=new VECTOR(A.y*B.z-A.z*B.y,A.z*B.x-A.x*B.z,A.x*B.y-B.x*A.y);
return C;
}
public static VECTOR unit(VECTOR A){
double sqt=Math.sqrt(A.x*A.x+A.y*A.y+A.z*A.z);
VECTOR C=new VECTOR(A.x/sqt,A.y/sqt,A.z/sqt);
return C;
}
public static VECTOR rotate(VECTOR A,VECTOR axis)
{
VECTOR B=new VECTOR(A.x,A.y,A.z);
double t=Math.atan(B.y/B.x);
if(B.x<0) t+=3.141592;
double l=Math.sqrt(B.x*B.x+B.y*B.y);
B.x=l*Math.cos(t+axis.z);
B.y=l*Math.sin(t+axis.z);
/////
t=Math.atan(B.z/B.y);
l=Math.sqrt(B.y*B.y+B.z*B.z);
if(B.y<0) t+=3.141592;
B.y=l*Math.cos(t+axis.x);
B.z=l*Math.sin(t+axis.x);

t=Math.atan(B.x/B.z);//´ÙÀ½
l=Math.sqrt(B.z*B.z+B.x*B.x);
if(B.z<0) t+=3.141592;
B.z=l*Math.cos(t+axis.y);
B.x=l*Math.sin(t+axis.y);
return B;
}
public static VECTOR absol(VECTOR xaxis,VECTOR yaxis,VECTOR var)
{
VECTOR A=new VECTOR(inner(xaxis,var).value(),inner(yaxis,var).value(),inner(unit(outer(xaxis,yaxis)),var).value());
return A;
}
public static VECTOR gop(VECTOR A,double b)
{
return new VECTOR(A.x*b,A.y*b,A.z*b);
}
public static VECTOR hap(VECTOR A,VECTOR B)
{
return new VECTOR(A.x+B.x,A.y+B.y,A.z+B.z);
}
public static VECTOR minus(VECTOR A,VECTOR B)
{
return new VECTOR(A.x-B.x,A.y-B.y,A.z-B.z);
}
public static double getAngle(VECTOR A,VECTOR B)
{
double theta=Math.acos(inner(A,B).value()/(A.value()*B.value()));
return theta;
}
};
