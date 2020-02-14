package world.bentobox.abc.dos;

import java.util.UUID;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;

/**
 * QR code content
 * @author tastybento
 *
 */
public class Code implements DataObject {

    @Expose
    private String uniqueId;
    @Expose
    private String payTo;
    @Expose
    private Long amount;
    @Expose
    private UUID id;
    @Expose
    private String command;
    @Expose
    private UUID nonce;
    @Expose
    private String hash;

    public Code(Code code) {
        this.uniqueId = code.uniqueId;
        this.payTo = code.payTo;
        this.amount = code.amount;
        this.id = code.id;
        this.command = code.command;
        this.nonce = UUID.randomUUID();
        this.hash = code.hash;
    }

    public Code() {
        this.nonce = UUID.randomUUID();
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.database.objects.DataObject#getUniqueId()
     */
    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.database.objects.DataObject#setUniqueId(java.lang.String)
     */
    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
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
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return the nonce
     */
    public UUID getNonce() {
        return nonce;
    }

    /**
     * @param nonce the nonce to set
     */
    public void setNonce(UUID nonce) {
        this.nonce = nonce;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Code [" + (uniqueId != null ? "uniqueId=" + uniqueId + ", " : "")
                + (payTo != null ? "payTo=" + payTo + ", " : "") + (amount != null ? "amount=" + amount + ", " : "")
                + (id != null ? "id=" + id + ", " : "")
                + (nonce != null ? "nonce=" + nonce : "") + "]";
    }
}
