import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution {

    static class Fraction {
        BigInteger num, den;

        Fraction(BigInteger n, BigInteger d) {
            if (d.signum() < 0) { n = n.negate(); d = d.negate(); }
            BigInteger g = n.gcd(d);
            if (g.signum() != 0) { n = n.divide(g); d = d.divide(g); }
            num = n;
            den = d;
        }

        Fraction add(Fraction o) {
            return new Fraction(
                num.multiply(o.den).add(o.num.multiply(den)),
                den.multiply(o.den)
            );
        }

        Fraction multiply(Fraction o) {
            return new Fraction(num.multiply(o.num), den.multiply(o.den));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java Solution <path-to-testcase.json>");
            return;
        }

        String content = new String(Files.readAllBytes(Paths.get(args[0])));

        int n = Integer.parseInt(extract(content, "\"n\"\\s*:\\s*(\\d+)"));
        int k = Integer.parseInt(extract(content, "\"k\"\\s*:\\s*(\\d+)"));

        Pattern pointPattern = Pattern.compile(
            "\"(\\d+)\"\\s*:\\s*\\{\\s*\"base\"\\s*:\\s*\"(\\d+)\"\\s*,\\s*\"value\"\\s*:\\s*\"([0-9a-zA-Z]+)\"\\s*\\}"
        );
        Matcher matcher = pointPattern.matcher(content);

        Map<Long, BigInteger> points = new TreeMap<>();

        while (matcher.find()) {
            long x = Long.parseLong(matcher.group(1));
            int base = Integer.parseInt(matcher.group(2));
            String value = matcher.group(3);
            BigInteger y = decodeBase(value, base);
            points.put(x, y);
        }
    
        List<Long> xs = new ArrayList<>(points.keySet());
        Collections.sort(xs);

        List<BigInteger> xList = new ArrayList<>();
        List<BigInteger> yList = new ArrayList<>();
        for (int i = 0; i < k && i < xs.size(); i++) {
            xList.add(BigInteger.valueOf(xs.get(i)));
            yList.add(points.get(xs.get(i)));
        }

        Fraction result = new Fraction(BigInteger.ZERO, BigInteger.ONE);

        for (int i = 0; i < xList.size(); i++) {
            Fraction term = new Fraction(yList.get(i), BigInteger.ONE);
            for (int j = 0; j < xList.size(); j++) {
                if (i == j) continue;
                BigInteger numerator = xList.get(j).negate();
                BigInteger denominator = xList.get(i).subtract(xList.get(j));
                term = term.multiply(new Fraction(numerator, denominator));
            }
            result = result.add(term);
        }

        System.out.println("n = " + n + ", k = " + k);
        System.out.println("Constant term (c) = " + result.num.divide(result.den));
    }

    static String extract(String content, String regex) {
        Matcher m = Pattern.compile(regex).matcher(content);
        if (m.find()) return m.group(1);
        throw new RuntimeException("Could not find pattern: " + regex);
    }

    static BigInteger decodeBase(String value, int base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger b = BigInteger.valueOf(base);
        for (char c : value.toCharArray()) {
            int digit = Character.digit(c, base);
            result = result.multiply(b).add(BigInteger.valueOf(digit));
        }
        return result;
    }
}
