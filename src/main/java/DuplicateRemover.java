import java.util.Arrays;

public class DuplicateRemover {
    public static void main(String[] args) {
        System.out.println(new DuplicateRemover().removeDuplicateCharacters("jjjjijjjj"));
        System.out.println(new DuplicateRemover().removeDuplicateCharacters("pqxrpx"));
        System.out.println(new DuplicateRemover().removeDuplicateCharacters("edcbabcde"));
    }

    public String removeDuplicateCharacters(final String input) {
        final char[] stack = new char[input.length()];
        int top = 0;
        final int frequency[] = new int[256];
        for (final char c : input.toCharArray()) {
            frequency[c]++;
        }
        final char[] charArray = input.toCharArray();
        for (int i1 =  charArray.length-1; i1 >=0; i1--) {
            final char c = charArray[i1];
            frequency[c]--;
            if (top == 0 || stack[top - 1] < c) {
                stack[top++] = c;
            } else {
                final char[] tempStack = new char[input.length()];
                int newTop = 0;
                for (int i = 0; i < top; i++) {
                    if (frequency[i] <= 0) {
                        tempStack[newTop++] = stack[i];
                    }
                }
                System.arraycopy(tempStack, 0, stack, 0, newTop);
                top = newTop;
                if (frequency[c] >= 0) {
                    stack[top++] = c;
                }
            }
        }
        return new String(Arrays.copyOf(stack, top));
    }
}
