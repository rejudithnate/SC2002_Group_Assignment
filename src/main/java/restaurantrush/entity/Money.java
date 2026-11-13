package restaurantrush.entity;

final class Money {

    private Money() {
    }

    static int percentage(int cents, int percentage) {
        if (cents < 0 || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Invalid amount");
        }

        return (int) (((long) cents * percentage + 50) / 100);
    }
}