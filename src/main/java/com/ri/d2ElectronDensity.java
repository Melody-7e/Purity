package com.ri;

import static com.ri.meta.ProjectType.IMAGE;
import static com.ri.meta.ProjectType.INT_VEC2;
import static com.ri.meta.ProjectType.ProjectTypeCode.FUNCTION;
import static java.lang.Math.*;

import com.ri.meta.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class d2ElectronDensity {
    // Bohr radius in arbitrary units (e.g., Angstroms, a.u.).
    private static final double A0 = 1.0;

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static void main(String[] args) throws Exception {
        // @formatter:off
        String          _clazzName  = d2ElectronDensity.class.getSimpleName();
        ProjectType     type        = new ProjectType(FUNCTION, IMAGE, INT_VEC2);
        ProjectPD       pd          = ProjectPD.LEFT;
        ProjectCategory category    = ProjectCategory.CLASS_T;
        byte            id          = (byte) 17;
        String          name        = _clazzName;
        ProjectState    state       = ProjectState.FAILED;
        // @formatter:on

        ProjectName projectName = new ProjectName(type, pd, category, id, name, state);
        Projects.getInstance().checkName(projectName);

        System.out.print("================================ ");
        System.out.println(projectName.getFullName());

        execute(projectName);

        System.out.println();
        System.out.println("SUCCESS");
    }

    private static void execute(ProjectName projectName) throws IOException {
        int size = 1024;

        int n = 4;
        int m = 1;


        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                float x = (float) ((j - size / 2d) / (4d * PI * size)) * 640;
                float y = (float) ((i - size / 2d) / (4d * PI * size)) * 640;

                double r = sqrt(x * x + y * y);
                double phi = atan2(y, x);

                double radialResult = radialFunction(n, m, r);
                Complex angularResult = angularFunction(m, phi);
                Complex waveFunction = new Complex(radialResult, 0).multiply(angularResult);

                float b = (float) (waveFunction.real * waveFunction.real + waveFunction.imaginary * waveFunction.imaginary);
                float h = (float) atan2(waveFunction.real, waveFunction.imaginary);

                img.setRGB(j, i, Color.HSBtoRGB((float) (signum(waveFunction.real)/2f - 0.5f), 1F,
                        (float) Math.abs(waveFunction.real)));
            }
        }

        ImageIO.write(img, "png", projectName.getFile("png"));
    }

    /**
     * Calculates the radial part of the wave function, $R_{n,m}(r)$.
     * <p>
     * $$ R_{n,m}(r) = \frac{\sqrt{(n-|m|-1)!}}{n a_0 \sqrt{(n+|m|-1)!}} \left(\frac{r}{a_0}\right)^{|m|} e^{-r/(n a_0)} L_{n-|m|-1}^{2|m|}\left(\frac{2r}{n a_0}\right) $$
     *
     * @param n The principal quantum number ($n \ge 1$).
     * @param m The magnetic quantum number (integer, $|m| < n$).
     * @param r The radial distance from the nucleus (in units of Bohr radius).
     * @return The value of the radial function.
     */
    private static double radialFunction(int n, int m, double r) {
        if (n < 1 || abs(m) >= n) {
            throw new IllegalArgumentException("Invalid quantum numbers for 2D: n=" + n + ", m=" + m);
        }

        // Radial parameter for the polynomial
        double rho = 2.0 * r / (n * A0);

        // Normalization constant
        double norm = sqrt(factorial(n - abs(m) - 1.0) / factorial(n + abs(m) - 1.0)) / (n * A0);

        // The power and exponential terms
        double term2 = pow(r / A0, abs(m)) * exp(-r / (n * A0));

        // The associated Laguerre polynomial
        double laguerreTerm = associatedLaguerre(n - abs(m) - 1, 2 * abs(m), rho);

        return norm * term2 * laguerreTerm;
    }

    /**
     * Calculates the angular part of the wave function, $\Phi_m(\phi)$.
     * <p>
     * $$ \Phi_m(\phi) = \frac{1}{\sqrt{2\pi}} e^{im\phi} $$
     *
     * @param m   The magnetic quantum number.
     * @param phi The azimuthal angle $\phi$ in radians ($0 \le \phi < 2\pi$).
     * @return A Complex object representing the angular function.
     */
    private static Complex angularFunction(int m, double phi) {
        double normalization = 1.0 / sqrt(2.0 * PI);
        double realPart = normalization * cos(m * phi);
        double imaginaryPart = normalization * sin(m * phi);
        return new Complex(realPart, imaginaryPart);
    }

    /**
     * Calculates the associated Laguerre polynomial, $L_p^q(x)$, using a recurrence relation.
     *
     * @param p The order p of the polynomial.
     * @param q The order q of the polynomial.
     * @param x The variable.
     * @return The value of the polynomial.
     */
    private static double associatedLaguerre(int p, int q, double x) {
        if (p < 0) {
            return 0;
        }
        if (p == 0) {
            return 1;
        }
        if (p == 1) {
            return -x + q + 1;
        }

        double l_prev2 = 1;
        double l_prev1 = -x + q + 1;
        double l_current = 0;

        for (int k = 2; k <= p; k++) {
            l_current = ((2.0 * k - 1.0 + q - x) * l_prev1 - (k - 1.0 + q) * l_prev2) / k;
            l_prev2 = l_prev1;
            l_prev1 = l_current;
        }
        return l_current;
    }

    /**
     * Calculates the factorial of a non-negative integer.
     *
     * @param n The number.
     * @return The factorial of n.
     */
    private static double factorial(double n) {
        if (n < 0) {
            return 0;
        }
        double result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Represents a complex number with real and imaginary parts.
     */
    private static class Complex {
        public double real;
        public double imaginary;

        public Complex(double real, double imaginary) {
            this.real = real;
            this.imaginary = imaginary;
        }

        public Complex multiply(Complex other) {
            return new Complex(this.real * other.real - this.imaginary * other.imaginary,
                    this.real * other.imaginary + this.imaginary * other.real);
        }
    }
}
