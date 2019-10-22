import java.io.Serializable;

public class Tuple <T1 extends Serializable, T2 extends Serializable> implements Serializable {
    public final T1 code;
    public final T2 token;

    public Tuple(T1 code, T2 token) {
        this.code = code;
        this.token = token;
    }
}

