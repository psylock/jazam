package FingerPrinting.computation;


public class FastFourierTransform {

    public static int CHUNK_SIZE = 32768; /** For a 400ms slice */
	//public static int CHUNK_SIZE = 4096;
 
	
	public static Complex[] fourierTransform(short[] sample){
		int totalSize = sample.length;
		
		int amountPossible = 0;
		
		while (amountPossible == 0 && CHUNK_SIZE > 0){
			amountPossible = totalSize/CHUNK_SIZE;
			CHUNK_SIZE /= 2;
		}
		
		CHUNK_SIZE *= 2;
		
		
		Complex[] complex = new Complex[CHUNK_SIZE];
			
		for (int i = 0; i < CHUNK_SIZE; ++i){
			complex[i] = new Complex(sample[i],0);
		}
			
		return fft(complex);		
	}
	
	 // compute the FFT of x[], assuming its length is a power of 2
    private static Complex[] fft(Complex[] x) {
        int N = x.length;
      

        // base case
        if (N == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[(int)(N/2)];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

}
