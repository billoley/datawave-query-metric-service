package datawave.microservice.querymetrics;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter implements Serializable {
    
    private static final long serialVersionUID = 2L;
    
    @XmlElement(name = "name")
    private String parameterName;
    @XmlElement(name = "value")
    private String parameterValue;
    
    public Parameter() {}
    
    public Parameter(@JsonProperty String name, @JsonProperty String value) {
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
