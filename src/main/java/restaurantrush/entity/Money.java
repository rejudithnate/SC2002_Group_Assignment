package restaurantrush.entity;

final class Money {
    private Money() {
    }

    static int percentage(int cents, int percentage) {
        if (cents < 0 || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Invalid amount");
        }

        // Add half a cent before division to round to the nearest cent.
        return (int) (((long) cents * percentage + 50) / 100);
    }
}
