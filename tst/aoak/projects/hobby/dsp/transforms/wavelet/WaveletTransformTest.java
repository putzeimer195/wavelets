package aoak.projects.hobby.dsp.transforms.wavelet;

import aoak.projects.hobby.dsp.utils.PlottingUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.complex.Complex;
import org.junit.Assert;
import org.junit.Test;

public class WaveletTransformTest {

    @Test
    public void convolveAndSubsampleTest() {

        Complex[] s = new Complex[3];
        s[0] = new Complex(1);
        s[1] = new Complex(2);
        s[2] = new Complex(3);
        Double[] h = new Double[] {5.0, 6.0,7.0, 8.0};
        Complex[] e = new Complex[3];
        e[0] = new Complex(5);
        e[1] = new Complex(34);
        e[2] = new Complex(37);

        Assert.assertArrayEquals(e, WaveletTransform.convolveAndSubsample(s, h));
    }

    @Test
    public void dwtTest() throws IOException {
        Complex[] input = new Complex[8];
        input[0] = Complex.ZERO;
        input[1] = new Complex(0.7071);
        input[2] = new Complex(1);
        input[3] = new Complex(0.7071);
        input[4] = Complex.ZERO;
        input[5] = new Complex(-0.7071);
        input[6] = new Complex(-1);
        input[7] = new Complex(-0.7071);

        Complex[][] trans = WaveletTransform.dwt(input, Wavelet.DB3);
        Complex[] recon = WaveletTransform.iDwt(trans[0], trans[1], Wavelet.DB3);
        PlottingUtils.savePlot(input, "inWave");
        PlottingUtils.savePlot(recon, "outWave");
    }

    @Test
    public void longDwtTest() throws IOException {
         List<Complex> sigFile = Files.readAllLines((Paths.get("tst/aoak/projects/hobby/dsp/transforms/wavelet/data/sine.txt"))).
                                     stream().
                                     map(val -> new Complex(Double.valueOf(val))).
                                     collect(Collectors.toList());
         Complex[] signal = new Complex[sigFile.size()];
         signal = sigFile.toArray(signal);
         Complex[][] trans = WaveletTransform.dwt(signal, Wavelet.DB3);
         Complex[] recon = WaveletTransform.iDwt(trans[0], trans[1], Wavelet.DB3);
         PlottingUtils.savePlot(signal, "longSine");
         PlottingUtils.savePlot(recon, "reconLongSine");
    }

    @Test
    public void waveletDecompositionSimpleTest() {
        Complex[] input = new Complex[8];
        input[0] = Complex.ZERO;
        input[1] = new Complex(0.7071);
        input[2] = new Complex(1);
        input[3] = new Complex(0.7071);
        input[4] = Complex.ZERO;
        input[5] = new Complex(-0.7071);
        input[6] = new Complex(-1);
        input[7] = new Complex(-0.7071);

        Complex[][] trans = WaveletTransform.dwt(input, Wavelet.DB3);
        Complex[][] dec = WaveletTransform.waveletDecomposition(input, Wavelet.DB3, 1);

        Assert.assertArrayEquals(trans[0], dec[0]);
        Assert.assertArrayEquals(trans[1], dec[1]);
    }

    @Test
    public void waveletReconstructionSimpleTest() {
        Complex[] input = new Complex[8];
        input[0] = Complex.ZERO;
        input[1] = new Complex(0.7071);
        input[2] = new Complex(1);
        input[3] = new Complex(0.7071);
        input[4] = Complex.ZERO;
        input[5] = new Complex(-0.7071);
        input[6] = new Complex(-1);
        input[7] = new Complex(-0.7071);

        Complex[][] trans = WaveletTransform.dwt(input, Wavelet.DB3);
        Complex[][] dec = WaveletTransform.waveletDecomposition(input, Wavelet.DB3, 1);

        Assert.assertArrayEquals(WaveletTransform.iDwt(trans[0], trans[1], Wavelet.DB3),
                                 WaveletTransform.waveletReconstruction(dec, Wavelet.DB3));
    }

    @Test
    public void longWaveletDecompositionTest() throws IOException {
         List<Complex> sigFile = Files.readAllLines((Paths.get("tst/aoak/projects/hobby/dsp/transforms/wavelet/data/sine.txt"))).
                                     stream().
                                     map(val -> new Complex(Double.valueOf(val))).
                                     collect(Collectors.toList());
         Complex[] signal = new Complex[sigFile.size()];
         signal = sigFile.toArray(signal);
         Complex[][] trans = WaveletTransform.waveletDecomposition(signal, Wavelet.DB3, 2);
         Complex[] recon = WaveletTransform.waveletReconstruction(trans, Wavelet.DB3);
         PlottingUtils.savePlot(signal, "longSine");
         PlottingUtils.savePlot(recon, "reconLongSine");
    }
}
