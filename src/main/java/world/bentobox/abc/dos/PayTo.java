package world.bentobox.abc.dos;

import java.security.SecureRandom;

import com.google.gson.annotations.Expose;

public class PayTo {

    @Expose
    String payTo;
    @Expose
    String payFrom;
    @Expose
    final long nonce;
    @Expose
    String password;
    @Expose
    Long amount = 0L;

    SecureRandom sr = new SecureRandom();
    /**
     * Construct a pay to object with a secure random nonce
     */
    public PayTo() {
        nonce = sr.nextLong();
    }
    /**
     * @return the amount
     */
    public Long getAmount() {
        return amount;
    }
    /**
     * @param amount the amount to set
     */
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    /**
     * @return the nonce
     */
    public long getNonce() {
        return nonce;
    }
    /**
     * @return the payTo
     */
    public String getPayTo() {
        return payTo;
    }
    /**
     * @param payTo the payTo to set
     */
    public void setPayTo(String payTo) {
        this.payTo = payTo;
    }
    /**
     * @return the payFrom
     */
    public String getPayFrom() {
        return payFrom;
    }
    /**
     * @param payFrom the payFrom to set
     */
    public void setPayFrom(String payFrom) {
        this.payFrom = payFrom;
    }
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }


}
