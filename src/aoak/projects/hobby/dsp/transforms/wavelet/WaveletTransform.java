package aoak.projects.hobby.dsp.transforms.wavelet;

import static aoak.projects.hobby.dsp.utils.SignalProcessingUtils.*;

import org.apache.commons.math3.complex.Complex;

import aoak.projects.hobby.dsp.utils.ArrayUtils;
import aoak.projects.hobby.dsp.utils.SignalProcessingUtils;

public class WaveletTransform {

    /**
     * Calculate discrete wavelet transform using given wavelet to construct
     * QMF pair
     * @param signal
     * @param wavelet
     * @return
     */
    public static Complex[][] dwt(Complex[] signal, Wavelet wavelet) {
        Double[] waveletCoeffs = wavelet.getWavelet();
        double norm = SignalProcessingUtils.getNorm(waveletCoeffs);
        Double[] lo_D = SignalProcessingUtils.getReverse(ArrayUtils.mapInPlace(waveletCoeffs, ele -> ele/norm));
        return dwt(signal, lo_D);
    }

    /**
     * Calculate discrete wavelet transform by convolution and sub-sampling
     * with the input filter as low pass filter and it's QMF pair.
     * @param signal
     * @param lpFilter
     * @return
     */
    public static Complex[][] dwt(Complex[] signal, Double[] lpFilter) {

        int numberOfDecompositions = 2;
        Complex[][] result = new Complex[numberOfDecompositions][];

        // now get the QMF pair filter
        Double[] hpFilter = getQMF(lpFilter);

        result[0] = convolveAndSubsample(signal, lpFilter);
        result[1] = convolveAndSubsample(signal, hpFilter);
        return result;
    }

    /**
     * Compute the discrete wavelet transform in cascaded manner. The signal
     * is first decomposed into approximation and details. The approximations
     * are further decomposed into lower level approximations and details. This
     * continues until we get numLevels details and one approximation
     * @param signal
     * @param wavelet
     * @param numLevels
     * @return a two dimensional array of numLevels+1 x ... length
     */
    public static Complex[][] waveletDecomposition(Complex[] signal, Wavelet wavelet, int numLevels) {

        int numPossibleDecompositions = (int) (Math.log10(signal.length)/Math.log10(2));
        if (numLevels > numPossibleDecompositions) {
            throw new IllegalArgumentException("Can't decompose more than " + numPossibleDecompositions + " times");
        }
        Complex[][] result = new Complex[numLevels+1][];
        for (int i = result.length - 1; i >= 1; i--) {
            Complex[][] decomposition = dwt(signal, wavelet);
            // preserve the details
            result[i] = decomposition[1];
            // decompose the approximation further
            signal = decomposition[0];
        }
        // the final stage approximation
        result[0] = signal;
        return result;
    }

    /**
     * Compute inverse dwt from given approximation, and details using the given
     * wavelet to construct reconstruction QMF filter pair
     * @param approx
     * @param details
     * @param wavelet
     * @return
     */
    public static Complex[] iDwt(Complex[] approx, Complex[] details, Wavelet wavelet) {
        Double[] waveletCoeffs = wavelet.getWavelet();
        double norm = SignalProcessingUtils.getNorm(waveletCoeffs);
        Double[] lo_R = ArrayUtils.mapInPlace(waveletCoeffs, ele -> ele/norm);
        return iDwt(approx, details, lo_R);
    }

    /**
     * Compute inverse dwt from given approximation, detail and low pass filter.
     * (High pass filter is generated by the function using the LPF).
     * @param approx
     * @param details
     * @param lpFilter
     * @return
     */
    public static Complex[] iDwt(Complex[] approx, Complex[] details, Double[] lpFilter) {

        Double[] hpFilter = getQMF(lpFilter);

        approx = SignalProcessingUtils.upsampleWithInterpolation(approx, approx.length * 2);
        details = SignalProcessingUtils.upsampleWithInterpolation(details, details.length * 2);
        // approx = upsample(approx, 2);
        // details = upsample(details, 2);
        Complex[] regeneratedSignal = ArrayUtils.merge(conv(approx, lpFilter), conv(details, hpFilter), (a, b) -> a.add(b));
        return regeneratedSignal;
    }

    public static Complex[] waveletReconstruction(Complex[][] transform, Wavelet wavelet) {

        int numLevels = transform.length - 1;
        if (numLevels == 0) {
            throw new IllegalArgumentException("Invalid transform matrix");
        }
        Complex[] signal = transform[0];
        for (int i = 1; i < transform.length; i++) {
            Complex[] details = transform[i];
            signal = iDwt(signal, details, wavelet);
        }
        return signal;
    }

    static Complex[] convolveAndSubsample(Complex[] signal, Double[] filter) {
        int N = signal.length;
        int K = filter.length;

        if (N == 1) {
            return signal;
        }
        if ((N+K-1) % 2 != 0) {
            filter = ArrayUtils.pad(filter, 1, 0);
            K = filter.length;
        }

        Complex[] result = new Complex[(N+K-1)/2];
        ArrayUtils.mapInPlace(result, ele -> Complex.ZERO);

        /* filter[k], signal[n-k], result[n] result length is k+n-1
         * sum over all k
         */
        for (int n = 0; n < result.length; n++) {
            for (int k = Math.max(0, 2*n-N+1); k <= Math.min(2*n, K-1); k++) {
                result[n] = result[n].add(signal[2*n-k].multiply(filter[k]));
            }
        }
        return result;
    }
}
