package by.comrade.model;

public class Bill {

    private String orderId;

    private double total;

    private String comment;

    private int bonusPointsCount;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getBonusPointsCount() {
        return bonusPointsCount;
    }

    public void setBonusPointsCount(int bonusPointsCount) {
        this.bonusPointsCount = bonusPointsCount;
    }
}
