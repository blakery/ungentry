/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/*
 * This file was semi-automatically converted from the public-domain USGS PROJ source.
 */

/**
 * Fixed bug in inverse computation, no exception is thrown from inverse
 * projection if input is out of bounds, added comments,
 * Bernhard Jenny, May 26 2010
 */
package com.jhlabs.map.proj;

import java.awt.geom.*;
import com.jhlabs.map.*;

public class RobinsonProjection extends PseudoCylindricalProjection {

    /* note: following terms based upon 5 deg. intervals in degrees. */
    private final static double X[] = {
        1, -5.67239e-12, -7.15511e-05, 3.11028e-06,
        0.9986, -0.000482241, -2.4897e-05, -1.33094e-06,
        0.9954, -0.000831031, -4.4861e-05, -9.86588e-07,
        0.99, -0.00135363, -5.96598e-05, 3.67749e-06,
        0.9822, -0.00167442, -4.4975e-06, -5.72394e-06,
        0.973, -0.00214869, -9.03565e-05, 1.88767e-08,
        0.96, -0.00305084, -9.00732e-05, 1.64869e-06,
        0.9427, -0.00382792, -6.53428e-05, -2.61493e-06,
        0.9216, -0.00467747, -0.000104566, 4.8122e-06,
        0.8962, -0.00536222, -3.23834e-05, -5.43445e-06,
        0.8679, -0.00609364, -0.0001139, 3.32521e-06,
        0.835, -0.00698325, -6.40219e-05, 9.34582e-07,
        0.7986, -0.00755337, -5.00038e-05, 9.35532e-07,
        0.7597, -0.00798325, -3.59716e-05, -2.27604e-06,
        0.7186, -0.00851366, -7.0112e-05, -8.63072e-06,
        0.6732, -0.00986209, -0.000199572, 1.91978e-05,
        0.6213, -0.010418, 8.83948e-05, 6.24031e-06,
        0.5722, -0.00906601, 0.000181999, 6.24033e-06,
        0.5322, 0., 0., 0.
    };
    private final static double Y[] = {
        0, 0.0124, 3.72529e-10, 1.15484e-09,
        0.062, 0.0124001, 1.76951e-08, -5.92321e-09,
        0.124, 0.0123998, -7.09668e-08, 2.25753e-08,
        0.186, 0.0124008, 2.66917e-07, -8.44523e-08,
        0.248, 0.0123971, -9.99682e-07, 3.15569e-07,
        0.31, 0.0124108, 3.73349e-06, -1.1779e-06,
        0.372, 0.0123598, -1.3935e-05, 4.39588e-06,
        0.434, 0.0125501, 5.20034e-05, -1.00051e-05,
        0.4968, 0.0123198, -9.80735e-05, 9.22397e-06,
        0.5571, 0.0120308, 4.02857e-05, -5.2901e-06,
        0.6176, 0.0120369, -3.90662e-05, 7.36117e-07,
        0.6769, 0.0117015, -2.80246e-05, -8.54283e-07,
        0.7346, 0.0113572, -4.08389e-05, -5.18524e-07,
        0.7903, 0.0109099, -4.86169e-05, -1.0718e-06,
        0.8435, 0.0103433, -6.46934e-05, 5.36384e-09,
        0.8936, 0.00969679, -6.46129e-05, -8.54894e-06,
        0.9394, 0.00840949, -0.000192847, -4.21023e-06,
        0.9761, 0.00616525, -0.000256001, -4.21021e-06,
        1., 0., 0., 0
    };

    /**
     * number of spline segments
     */
    private final static int NODES = 18;
    /**
     * global scale factor applied to the graticule
     */
    private final static double FXC = 0.8487;
    /**
     * height-to-width factor of the graticule
     */
    private static final double HEIGHT_TO_WIDTH = 0.5072;
    /**
     * vertical scale factor
     */
    private final static double FYC = FXC * HEIGHT_TO_WIDTH * Math.PI; // 1.3523;
    /**
     * convert from latitude in radians to spline segment index
     */
    private final static double C1 = NODES / Math.PI * 2; // 11.45915590261646417544;
    /**
     * a spline segment corresponds to this latitude segment in radians
     */
    private final static double RC1 = Math.PI / 2 / NODES; // 0.08726646259971647884;
    private final static double EPS = 1e-8;


    public RobinsonProjection() {
    }

    /**
     * evaluate the cubic spline
     * @param a Spline coefficients for all segments
     * @param offset Position of segment to evaluate
     * @param z Where the segment is evaluated in degrees [0..RC1]
     * @return
     */
    private double poly(double[] a, int offset, double z) {
        return (a[offset] + z * (a[offset + 1] + z * (a[offset + 2] + z * a[offset + 3])));
    }

    public Point2D.Double project(double lplam, double lpphi, Point2D.Double xy) {
        final double phiAbs = Math.abs(lpphi);
        // compute the spline segment index
        int i = (int) Math.floor(phiAbs * C1);
        if (i >= NODES) {
            i = NODES - 1;
        }
        // evaluate spline segment at position dphi [0..RC1]
        final double dphi = Math.toDegrees(phiAbs - RC1 * i);
        i *= 4;
        xy.x = poly(X, i, dphi) * FXC * lplam;
        xy.y = poly(Y, i, dphi) * FYC;
        if (lpphi < 0.0) {
            xy.y = -xy.y;
        }
        return xy;
    }

    public Point2D.Double projectInverse(double x, double y, Point2D.Double lp) {
        int i;
        double t, t1;

        lp.x = x / FXC;
        lp.y = Math.abs(y / FYC);
        if (lp.y >= 1.0) {
            if (lp.y > 1.000001) {
                // return NaN values instead of throwing an exception
                // Bernhard Jenny, May 26 2010
                lp.x = Double.NaN;
                lp.y = Double.NaN;
                return lp;
                //throw new ProjectionException();
            } else {
                lp.y = y < 0. ? -MapMath.HALFPI : MapMath.HALFPI;
                lp.x /= X[4 * NODES];
            }
        } else {
            for (i = 4 * (int) Math.floor(lp.y * NODES);;) {
                if (Y[i] > lp.y) {
                    i -= 4;
                } else if (Y[i + 4] <= lp.y) {
                    i += 4;
                } else {
                    break;
                }
            }

            // commented out next line, as this is not needed
            // Bernhard Jenny, May 26 2010
            // t = 5. * (lp.y - Y[i]) / (Y[i + 4] - Y[i]);
            double Tc0 = Y[i];
            double Tc1 = Y[i + 1];
            double Tc2 = Y[i + 2];
            double Tc3 = Y[i + 3];

            // changed following line from
            // t = 5. * (lp.y - Tc0) / (Y[i + 1] - Tc0);
            // Must be Y[i + 4] instead of Y[i + 1] to reference the correct value.
            // Here, all parametes are stored in a one-dimenaional array, which
            // differes from proj4, which uses a two-dimensional array.
            // Bernhard Jenny, May 26 2010
            t = 5. * (lp.y - Tc0) / (Y[i + 4] - Tc0);
            Tc0 -= lp.y;
            for (;;) { // Newton-Raphson
                t -= t1 = (Tc0 + t * (Tc1 + t * (Tc2 + t * Tc3))) / (Tc1 + t * (Tc2 + Tc2 + t * 3. * Tc3));
                if (Math.abs(t1) < EPS) {
                    break;
                }
            }
            lp.y = Math.toRadians(5 * i + t);
            if (y < 0.) {
                lp.y = -lp.y;
            }
            lp.x /= poly(X, i, t);
        }
        return lp;
    }

    public boolean hasInverse() {
        return true;
    }

    public String toString() {
        return "Robinson";
    }
}