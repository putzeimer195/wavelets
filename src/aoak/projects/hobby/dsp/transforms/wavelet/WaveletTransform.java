package aoak.projects.hobby.dsp.transforms.wavelet;

import org.apache.commons.math3.complex.Complex;

import aoak.projects.hobby.dsp.transforms.utils.ArrayUtils;

public class WaveletTransform {

    public static final Double[] DB3 = new Double[] {0.2352, 0.5706, 0.3252, -0.0955, -0.0604, 0.0249};

    public static Complex[] convolve(Complex[] signal, Double[] filter) {
        int N = signal.length;
        int K = filter.length;

        if (N == 1) {
            return signal;
        }

        Complex[] result = new Complex[N+K-1];
        ArrayUtils.mapInPlace(result, ele -> Complex.ZERO);

        /* filter[k], signal[n-k], result[n] result length is k+n-1
         * sum over all k
         */
        for (int n = 0; n < N + K -1; n++) { // 0-6
            for (int k = Math.max(0, n-N+1); k <= Math.min(n, K-1); k++) {
                result[n] = result[n].add(signal[n-k].multiply(filter[k]));
            }
        }
        return result;
    }
}