import java.util.Random;

class KeyGenerator {	
	
	//Instance fields
	private int TotalRound;
	private int RealNumberPickedEachRound;
	private int PreliminaryRound;
	private int RandomUpBound;
	private int IterationUpBound;
	private double Parameter;
	private double InitialCondition;
	private double BitsWrite;
	private double[] IterationResult;
	
	//Constructor
	public KeyGenerator() {
		
		TotalRound=20000;
		//The speed of the CBEQKD protocol is Proportional to the RealNumberPickedEachRound.
		//You can adjust the RealNumberPickedEachRound to observe the change of the speed.
		//And it is recommend that RealNumberPickedEachRound<65536
		RealNumberPickedEachRound=500;		
		PreliminaryRound=40;
		RandomUpBound=65536;
		IterationUpBound=65536;
		Parameter=0.47859312;
		InitialCondition=0.69832514;
		BitsWrite=0;
		IterationResult=new double[IterationUpBound];
	}
	
	private double PLCM(double p,double x){
		
		if(x>0&&x<=p)return x/p;
		else if(x>p&&x<=0.5)return (x-p)/(0.5-p);
		else if(x>0.5&&x<1)return PLCM(p,1-x);
		else {
			System.out.println("Error Initial Condition!");
			return -1;
		}
	}
	
	//Choose a random number between PreliminaryRound and RandomUpBound
	private int RandomNumChoose(){
		
		int RandomResult=0;
		while(RandomResult<PreliminaryRound){
			Random RandomNum=new Random();
			RandomResult=RandomNum.nextInt()%(RandomUpBound-1);
		}
		return RandomResult;
	}
	
	private byte[] LongToByte(long LongNumber){
		
		byte[] res=new byte[8];
		res[7]=(byte)LongNumber;
		res[6]=(byte)(LongNumber>>>8);
		res[5]=(byte)(LongNumber>>>16);
		res[4]=(byte)(LongNumber>>>24);
		res[3]=(byte)(LongNumber>>>32);
		res[2]=(byte)(LongNumber>>>40);
		res[1]=(byte)(LongNumber>>>48);
		res[0]=(byte)(LongNumber>>>56);
		return res;
	}
	
	private byte[] DoubleToBit(double RealNumber)	{
		
		long Temp=Double.doubleToLongBits(RealNumber);
		byte[] res=LongToByte(Temp);
		return res;
	}
	
	private void DisPlay(byte[] TempArray){
		
		byte Temp=0;
		byte Bit=0;
		for(int i=7;i>1;i--)
		{
			for(int j=0;j<8;j++){
				Temp=TempArray[i];
				Bit=(byte) (Temp & 0x01);
				System.out.print(Bit+" ");
				TempArray[i]=(byte) (Temp>>1);
			}			
		}
		for(int i=0;i<4;i++)
		{
			Temp=TempArray[1];
			Bit=(byte) (Temp & 0x01);
			System.out.print(Bit+" ");
			TempArray[1]=(byte) (Temp>>1);
		}
	}	
	
	public void KeyGeneration(){
		
		System.out.println("Key Generationg...");
		
		for(int i=0;i<TotalRound;i++){
			
			//Initialize the iteration results buffer
			for(int k=0;k<IterationUpBound;k++)
			{
				IterationResult[k]=PLCM(Parameter,InitialCondition);
				InitialCondition=IterationResult[k];
				if(IterationResult[k]<=0||IterationResult[k]>=1)
					System.out.println("Error Iteration Result");
			}
			
			byte[] ByteTemp=new byte[8];
			
			//Choose iteration result to generate final key string. This is theoretical speed of
			//the protocol. Delete the comment symbol of the statement below, you will obtain 
			//practical speed of the protocol
			for(int j=0;j<RealNumberPickedEachRound;j++){				
				
				ByteTemp=DoubleToBit(IterationResult[RandomNumChoose()]);
				BitsWrite+=52;
			}
			
			//Re-choose parameter and initial condition
			InitialCondition=IterationResult[RandomNumChoose()];
			
			Parameter=IterationResult[RandomNumChoose()];
			while(Parameter==InitialCondition||Parameter==0.5)
				Parameter=IterationResult[RandomNumChoose()];
			if(Parameter>0.5)Parameter=Parameter-0.5;
			
			if(i%(TotalRound/100)==0){
				System.out.print(i/(TotalRound/100)+"%: ");
				ByteTemp=DoubleToBit(IterationResult[RealNumberPickedEachRound-1]);
				DisPlay(ByteTemp);
				System.out.println(" ");
			}			
		}
		System.out.println("Length of Bit String: "+BitsWrite);		
	}
	
	public double GetBitsWrite(){
		return BitsWrite;
	}
	
}	
	
public class SpeedAnalysis {
	
	public static void main(String[] args){
		
		KeyGenerator KG=new KeyGenerator();
		
		long StartTime=System.currentTimeMillis();
		KG.KeyGeneration();
		long EndTime=System.currentTimeMillis();
		
		System.out.println("Time:"+(EndTime-StartTime)/1000+"s");
		System.out.println("Speed:"+KG.GetBitsWrite()/((double)(EndTime-StartTime)/1000)/1024/1024+"Mb/s");
	}
	
}
