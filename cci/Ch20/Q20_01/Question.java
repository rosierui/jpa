package Q20_01;

public class Question {

	public static int add_no_arithm(int a, int b) {
		if (b == 0) return a;
		int sum = a ^ b; // add without carrying
		int carry = (a & b) << 1; // carry, but don�t add
		return add_no_arithm(sum, carry); // recurse
	}
	
	public static int randomInt(int n) {
		return (int) (Math.random() * n);
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			int a = randomInt(10);
			int b = randomInt(10);
			int sum = add_no_arithm(a, b);
			System.out.println(a + " + " + b + " = " + sum);
		}
	}

}