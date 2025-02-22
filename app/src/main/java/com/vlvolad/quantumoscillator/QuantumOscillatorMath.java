package com.vlvolad.quantumoscillator;

import android.util.Log;
import android.util.Pair;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Volodymyr on 28.04.2015.
 */
public class QuantumOscillatorMath {
    static public final double a = 5.2917720859e-11;
    double avt;
    volatile boolean realksi, sign;
    Poly ksiR, ksiTH;


    double HermiteH(int n, double x) {
        double H0, H1, ret;
        H1 = 0.;
        ret = 1.;
        for(int i=1;i<=n;i++)
        {
            H0 = H1;
            H1 = ret;
            ret = 2. * x * H1 - 2. * (i-1) * H0;
        }
        return ret;
    }


    double LaguerreL(int n, double alpha, double x) {
        double L0,L1,ret;
        L1 = 0.;
        ret = 1.;
        for(int i=1;i<=n;i++)
        {
            L0 = L1;
            L1 = ret;
            ret = ((2.*i - 1. + alpha - x)*L1 - (i - 1. + alpha)*L0)/i;
        }
        return ret;
    }

    double IntG(Poly p)
    {
        double ret = 0,t = 1;
        for(int i=0;i<p.st+1;i++)
        {
            ret += p.c[i]*t;
            t *= i+1;
        }
        ret = 1/ret;
        return ret;
    }

    double avR(Poly p)
    {
        double ret = 0,t = 1;
        p.multiplyxs(1);
        for(int i=0;i<p.st+1;i++)
        {
            ret += p.c[i]*t;
            t *= i+1;
        }
        return ret;
    }

    double IntTh(Poly p)
    {
        double ret = 0;
        for(int i=0;i<p.st+1;i++)
        if ((i & 1)==0)
        {
            ret += p.c[i]*2.0/(i+1);
        }
        ret = 1/ret;
        return ret;
    }

    double ksi2costh(double costh, int m) {
        double ret = 0, t = 1, t1 = costh;
        for(int i=0;i<ksiTH.st+1;i++) { ret += ksiTH.c[i]*t; t *= t1; }
        if (sign) ret *= -1;
        return ret*ret*Math.pow(1 - t1 * t1, m);
    }

    public double ksi(double x, double y, double z, int k, int l, int m) {
        double r = Math.sqrt(x * x + y * y + z * z);
        double costh = z / r;
        double phi = 0;
        if (realksi) phi = Math.atan2(y, x);
        double ret1,ret2,ret3,t1,t2;
        ret1 = ret2 = ret3 = 0;
        t1 = 1;
        ret1 = Math.sqrt(2. / Math.PI);
        ret1 *= Math.pow(2, k + 2*l + 3);
        for(int i=1;i<=k;++i) ret1 *= i;
        for(int i=2*k + 2*l + 1; i>0; i -= 2) ret1 /= i;
        ret1 = Math.sqrt(ret1);
        ret1 *= Math.exp(-r*r) * Math.pow(r, l)*LaguerreL(k,l+0.5,2*r*r);


        t1 = 1; t2 = costh;
        for(int i=0;i<ksiTH.st+1;i++) {
            ret2 += ksiTH.c[i] * t1;
            t1 *= t2;
        }
        ret2 *= Math.pow(Math.sqrt(1-costh*costh),m);
        if (sign) ret2 *= -1;

        if (m==0 || !realksi) ret3 = 1/Math.sqrt(2*Math.PI);
        else if (!sign) ret3 = Math.sin(m*phi)/Math.sqrt(Math.PI);
        else ret3 = Math.cos(m*phi)/Math.sqrt(Math.PI);
        return ret1*ret2*ret3;
    }

    public double ksi1dim(double x, int n) {
        double ret = 1.;
        double fact = 1.;
        for(int i = 1; i < n; ++i)
            fact *= i;
        ret  = 1. / Math.sqrt( Math.pow(2, n) * fact );
        ret *= Math.pow(2. / Math.PI, 1./4.);
        ret *= Math.exp(-x*x);
        ret *= HermiteH(n, Math.sqrt(2.) * x);
        return ret;
    }

    public double ksicartesian(double x, double y, double z, int nx, int ny, int nz) {
        return ksi1dim(x, nx) * ksi1dim(y, ny) * ksi1dim(z, nz);
    }

    public void setsign(boolean sn)
    {
        sign = sn;
    }
    public void setksimode(boolean mode)
    {
        realksi = mode;
    }
    public boolean getksimode()
    {
        return realksi;
    }
    public boolean getsign()
    {
        return sign;
    }
    public double getavt()
    {
        return avt;
    }

    public void prepareallpov(int k, int l, int m) {
        Poly L;
        L = Poly.Laguerre(k+l);
        L.derivative(2*l+1);//L = diff(L, 2*l+1);
        L.multiplyxs(l);//L = mults(L, l);
        ksiR = L;
        //rdens = L;
        L = Poly.mult(L, L);//L = mult(L, L);
        //L = mult(L, IntG(L));
        //L = mult(L, IntG(L));
        //distR = L;
        L.multiplyxs(2);//L = mults(L, 2);
        ksiR.multiply(Math.sqrt(IntG(L)));//ksiR = mult(ksiR, sqrt(IntG(L)));
        L.multiply(IntG(L));//L = mult(L, IntG(L));  //плотность распределения по r

        //distR = L;
        avt = avR(L);          //среднее расстояние(мат. ожидание)
        //rdensk = L;
        //genidn(k);
        //rdis = Frd(L);		   //функция распределения по r

        Poly Plm = new Poly(),Ptm = new Poly(),Ptm2 = new Poly();
        Ptm.c = new double[3];
        Ptm.st = 2;
        Ptm.c[0] = -1;
        Ptm.c[1] = 0;
        Ptm.c[2] = 1;

        Plm.c = new double[1];
        Plm.st = 0;
        Plm.c[0] = 1;
        for(int i=0;i<l;i++) Plm = Poly.mult(Plm, Ptm);//Plm = mult(Plm, Ptm);
        Plm.derivative(l+m);//Plm = diff(Plm, l+m);
        ksiTH = Plm;
        Plm = Poly.mult(Plm, Plm);//Plm = mult(Plm, Plm);
        Ptm.c[0] = 1;
        Ptm.c[2] = -1;
        Ptm2.c = new double[1];
        Ptm2.st = 0;
        Ptm2.c[0] = 1;
        for(int i=0;i<m;i++) Ptm2 = Poly.mult(Ptm2, Ptm);//Ptm2 = mult(Ptm2, Ptm);
        Plm = Poly.mult(Plm, Ptm2);//Plm = mult(Plm, Ptm2);
        //distTh = Plm;
        ksiTH.multiply(Math.sqrt(IntTh(Plm)));//ksiTH = mult(ksiTH, sqrt(IntTh(Plm)));
        Plm.multiply(IntTh(Plm));//Plm = mult(Plm, IntTh(Plm));   //плотность распредления по theta
        //thdis = Fthgen(Plm);           //функция распредления по theta
    }
    public double getEquiValue(double percent, int iterr, double rmax, int itercth, int k, int l, int m) {
        Log.d("QuantumOscillatorMath", "getEquiValue() Intro");
        Pair<Double, Double>[] vals;
        //vector< pair<double, double> > vals(0);
        //vals.reserve(iterr*itercth);
        vals = new Pair[iterr*itercth];
        double dcth = 1. / itercth;
        double dr = rmax / iterr;
        double r = 0., cth = 0.;
        double tmp = 0., ret1 = 0.;
        int counter = 0;
//        Log.d("HydrogenAtomMath", "getEquiValue() a");
        for(int ir=0;ir<iterr;++ir)
            for(int icth=0;icth<itercth;++icth)
            {
                r = dr*ir;
                cth = dcth*icth;
                ret1 = Math.sqrt(2. / Math.PI);
                ret1 *= Math.pow(2, k + 2*l + 3);
                for(int i=1;i<=k;++i) ret1 *= i;
                for(int i=2*k + 2*l + 1; i>0; i -= 2) ret1 /= i;
                //ret1 = sqrt(ret1);
                tmp = Math.exp(-r*r) * Math.pow(r, l)*LaguerreL(k,l+0.5,2*r*r);
                ret1 *= tmp*tmp;
                tmp = ret1 * ksi2costh(cth, m);
                vals[counter] = Pair.create(tmp, r);
                counter++;
                //vals.push_back(make_pair(tmp, r));
            }
//        Log.d("HydrogenAtomMath", "getEquiValue() b");
        Arrays.sort(vals, new Comparator<Pair<Double, Double> >() {
            @Override public int compare(Pair<Double, Double> x, Pair<Double, Double> y) {
                if (x.first > y.first) return 1;
                else if (x.first < y.first) return -1;
                else return 0;
            }
        });
//        Log.d("HydrogenAtomMath", "getEquiValue() c");
        //sort(vals.begin(), vals.end());
        int sti = counter-1;
        double tsum = 0., tvol = dr*dcth;
        while (sti>=0)
        {
            tsum += vals[sti].first*tvol*vals[sti].second*vals[sti].second;
            if (2.*tsum>percent) break;
            sti--;
        }
        if (sti<0) sti++;
//        Log.d("HydrogenAtomMath", Double.toString(vals[sti].first / 2. / Math.PI));
//        Log.d("HydrogenAtomMath", "getEquiValue() finish");
        //qDebug () << vals[sti] << " " << sti << "\k";
        return vals[sti].first / 2. / Math.PI;
    }

}
