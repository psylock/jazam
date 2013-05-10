package FingerPrinting.computation;

import java.util.ArrayList;

import sound.InputSoundDecoder;
import sound.exceptions.UnsuportedSampleRateException;

public class HashableSound {

	/** 
	 * Hashing variables
	 * 
	 */
	private final int LOWER_LIMIT = 40;
	private final int UPPER_LIMIT = 300;
	private final int FUZ_FACTOR = 2;

	private int[] RANGE = new int[]{LOWER_LIMIT,80,120,180, UPPER_LIMIT+1};

	private int requiredBytes1Second;
	
	private InputSoundDecoder _input = null;
	
	public HashableSound(String fileName) throws UnsuportedSampleRateException {
		try {
			this._input = new InputSoundDecoder(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch((int)this._input.getOutputSampleRate()){
		
		case 44100:
				this.requiredBytes1Second = 176375;
			break;
			
		case 22050:
				this.requiredBytes1Second = 88187;
			break;
			
		default:
			throw new  UnsuportedSampleRateException((int)this._input.getOutputSampleRate());
		}
	}
	
	/** 
	 * Calculate _input sound hashes
	 * @param iSecondStep specifies how many seconds would be in a single sample
	 */
	public ArrayList<Long> calculateHashesPerSecond(int iMiliseconds){
		
		int bytesToRetrieve = (iMiliseconds*this.requiredBytes1Second)/1000;
		
		if (_input != null){
			ArrayList<Long> hashes = new ArrayList<Long>();
			
			byte[] buff = null;
			while( (buff = _input.getSamples(bytesToRetrieve)) != null ){
		
				Complex[] fftResult = FastFourierTransform.fourierTransform(byteToShortArray(buff));
				hashes.add(filterAndHash(fftResult));
			}
			return hashes;
		}
		else
			throw new RuntimeException("No input specified.");
	}
	
	public short[] byteToShortArray(byte[] array){
		short[] result = new short[array.length/2];
		
		for(int i = 0; i < result.length;++i){
			byte lo = array[i*2];
			byte hi = array[i*2+1];
			
			short val=(short)( ((hi&0xFF)<<8) | (lo&0xFF) );

			result[i] = val;
		}
		
		return result;
	}
	
	public byte[] toMono(byte[] stereo){
		byte[] mono = new byte[stereo.length/2];
		
		int HI = 1; int LO = 0; /** Big endian, or little endian, who knows */

		for (int i = 0 ; i < mono.length/2; ++i){
			int left = (stereo[i * 4 + HI] << 8) | (stereo[i * 4 + LO] & 0xff);
	        int right = (stereo[i * 4 + 2 + HI] <<8) | (stereo[i * 4 + 2 + LO] & 0xff);
	        int avg = (left + right) / 2;
	        mono[i * 2 + HI] = (byte)((avg >> 8) & 0xff);
	        mono[i * 2 + LO] = (byte)(avg & 0xff);
		}
		
		return mono;
	}

	private long filterAndHash(Complex[] fft){
		
		
		double[] highscores = new double[RANGE.length-1];
		int[] recordPoints = new int[RANGE.length-1];
		
		for (int i = 0; i < highscores.length; ++i){
			highscores[i] = -1;
			recordPoints[i] = -1;
		}
			
		for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT-1; freq++){
			double mag = Math.log(fft[freq].abs() +1);
					
			int index = getIndex(freq);
					
			if (mag > highscores[index]){
				highscores[index] = mag;
				recordPoints[index] = freq;
			}
							
		}
		
		return hash(recordPoints);
	}
	
	private long hash(int []p){
		int multiplyFactor = 100;
		int currentMultiplier = 1;
		long sum = 0;
		
		for (int i = 0; i < p.length; ++i){
			sum += (p[i] - (p[i]%FUZ_FACTOR)) * currentMultiplier;
			currentMultiplier *= multiplyFactor;
		}
		return sum;
	}
	
	private int getIndex(int freq){
		int i = 0;
		while (i < RANGE.length-1){
			if (freq >= RANGE[i] && freq < RANGE[i+1])
				return i;
			i++;
		}
		return i;	
	}
}
