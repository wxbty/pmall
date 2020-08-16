package ink.zfei.domain;

public class Result {


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private int code;
    private String message;

    public static Result sucess() {
        Result result = new Result();
        result.setCode(0);
        result.setMessage("sucess");
        return result;
    }

}
