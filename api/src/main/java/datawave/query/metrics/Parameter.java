package datawave.query.metrics;

import java.io.Serializable;

public class Parameter implements Serializable {
    
    private static final long serialVersionUID = 2L;
    
    private String parameterName;
    private String parameterValue;
    
    public Parameter() {}
    
    public Parameter(String name, String value) {
        this.parameterName = name;
        this.parameterValue = value;
    }
    
    public String getParameterName() {
        return parameterName;
    }
    
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
    
    public String getParameterValue() {
        return parameterValue;
    }
    
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("[name=").append(this.parameterName);
        sb.append(",value=").append(this.parameterValue).append("]");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (null == o)
            return false;
        if (!(o instanceof Parameter))
            return false;
        if (this == o)
            return true;
        Parameter other = (Parameter) o;
        if (this.getParameterName().equals(other.getParameterName()) && this.getParameterValue().equals(other.getParameterValue()))
            return true;
        else
            return false;
    }
    
    @Override
    public int hashCode() {
        return getParameterName() == null ? 0 : getParameterName().hashCode();
    }
}
