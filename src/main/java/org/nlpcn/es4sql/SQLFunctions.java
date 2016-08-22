package org.nlpcn.es4sql;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.elasticsearch.common.collect.Tuple;
import org.nlpcn.es4sql.domain.KVValue;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by allwefantasy on 8/19/16.
 */
public class SQLFunctions {

    //Groovy Built In Functions
    public final static Set<String> buildInFunctions = Sets.newHashSet(
            "exp", "log", "log10", "sqrt", "cbrt", "ceil", "floor", "rint", "pow", "round",
            "random", "abs", "split", "concat_ws", "substring", "trim",
            "add", "minus", "multiply", "divide"
    );


    public static Tuple<String, String> function(String methodName, List<KVValue> paramers, String name) {
        Tuple<String, String> functionStr = null;
        switch (methodName) {
            case "split":
                if (paramers.size() == 3) {
                    functionStr = split(paramers.get(0).value.toString(),
                            paramers.get(1).value.toString(),
                            Integer.parseInt(paramers.get(2).value.toString()), name);
                } else {
                    functionStr = split(paramers.get(0).value.toString(),
                            paramers.get(1).value.toString(),
                            name);
                }

                break;

            case "concat_ws":
                List<String> result = Lists.newArrayList();
                for (int i = 1; i < paramers.size(); i++) {
                    result.add(paramers.get(i).toString());
                }
                functionStr = concat_ws(paramers.get(0).value.toString(), result, name);

                break;

            case "floor":
                functionStr = floor(paramers.get(0).value.toString(), name);
                break;

            case "round":
                functionStr = round(paramers.get(0).value.toString(), name);
                break;
            case "log":
                functionStr = log(paramers.get(0).value.toString(), name);
                break;

            case "log10":
                functionStr = log10(paramers.get(0).value.toString(), name);
                break;

            case "sqrt":
                functionStr = sqrt(paramers.get(0).value.toString(), name);
                break;

            case "substring":
                functionStr = substring(paramers.get(0).value.toString(),
                        Integer.parseInt(paramers.get(1).value.toString()),
                        Integer.parseInt(paramers.get(2).value.toString())
                        , name);
                break;
            case "trim":
                functionStr = trim(paramers.get(0).value.toString(), name);
                break;

            case "add":
                functionStr = add(paramers.get(0).value.toString(), paramers.get(1).value.toString());
                break;

            case "subtract":
                functionStr = subtract(paramers.get(0).value.toString(), paramers.get(1).value.toString());
                break;
            case "divide":
                functionStr = divide(paramers.get(0).value.toString(), paramers.get(1).value.toString());
                break;

            case "multiply":
                functionStr = multiply(paramers.get(0).value.toString(), paramers.get(1).value.toString());
                break;
            case "modulus":
                functionStr = modulus(paramers.get(0).value.toString(), paramers.get(1).value.toString());
                break;

            default:

        }
        return functionStr;
    }

    public static String random() {
        return Math.abs(new Random().nextInt()) + "";
    }

    public static Tuple<String, String> concat_ws(String split, List<String> columns, String valueName) {
        String name = "concat_ws_" + random();

        List<String> result = Lists.newArrayList();

        for (String strColumn : columns) {
            //here we guess this is not column,but a function
            if (strColumn.startsWith("def ")) {
                result.add(strColumn);
            } else {
                result.add("doc['" + strColumn + "'].value");
            }


        }
        return new Tuple(name, "def " + name + " =" + Joiner.on("+'" + split + "'+").join(result));

    }


    //split(Column str, java.lang.String pattern)
    public static Tuple<String, String> split(String strColumn, String pattern, int index, String valueName) {
        String name = "split_" + random();
        if (valueName == null) {
            return new Tuple(name, "def " + name + " = doc['" + strColumn + "'].value.split('" + pattern + "')[" + index + "]");
        } else {
            return new Tuple(name, strColumn + "; def " + name + " = " + valueName + ".split('" + pattern + "')[" + index + "]");
        }

    }


    private static String extractName(String script) {
        String[] variance = script.split(";");
        String newScript = variance[variance.length - 1];
        if (newScript.trim().startsWith("def ")) {
            //for now ,if variant is string,then change to double.
            return newScript.substring(4).split("=")[0].trim();
        } else return newScript;
    }

    //cast(year as int)

    private static String convertType(String script) {

        String[] variance = script.split(";");
        String newScript = variance[variance.length - 1];
        if (newScript.trim().startsWith("def ")) {
            //for now ,if variant is string,then change to double.
            String temp =  newScript.substring(4).split("=")[0].trim();
            return " if( " + temp + " instanceof String) " + temp + "=" + temp.trim() + ".toDouble() ";
        } else return "";



    }


    public static Tuple<String, String> add(String a, String b) {
        return binaryOpertator("add", "+", a, b);
    }

    public static Tuple<String, String> modulus(String a, String b) {
        return binaryOpertator("modulus", "%", a, b);
    }

    public static Tuple<String, String> subtract(String a, String b) {
        return binaryOpertator("subtract", "-", a, b);
    }

    public static Tuple<String, String> multiply(String a, String b) {
        return binaryOpertator("multiply", "*", a, b);
    }

    public static Tuple<String, String> divide(String a, String b) {
        return binaryOpertator("divide", "/", a, b);
    }

    public static Tuple<String, String> binaryOpertator(String methodName, String operator, String a, String b) {
        String name = methodName + "_" + random();

        return new Tuple(name, a + ";" + b + ";" + convertType(a) + ";" + convertType(b) + "; def " + name + " = " + extractName(a) + operator + extractName(b));
    }


    public static Tuple<String, String> log(String strColumn, String valueName) {

        return mathSingleValueTemplate("log", strColumn, valueName);

    }

    public static Tuple<String, String> log10(String strColumn, String valueName) {

        return mathSingleValueTemplate("log10", strColumn, valueName);

    }

    public static Tuple<String, String> sqrt(String strColumn, String valueName) {

        return mathSingleValueTemplate("sqrt", strColumn, valueName);

    }

    public static Tuple<String, String> round(String strColumn, String valueName) {

        return mathSingleValueTemplate("round", strColumn, valueName);

    }

    public static Tuple<String, String> trim(String strColumn, String valueName) {

        return strSingleValueTemplate("trim", strColumn, valueName);

    }

    public static Tuple<String, String> mathSingleValueTemplate(String methodName, String strColumn, String valueName) {
        String name = methodName + "_" + random();
        if (valueName == null) {
            return new Tuple(name, "def " + name + " = " + methodName + "(doc['" + strColumn + "'].value)");
        } else {
            return new Tuple(name, strColumn + ";def " + name + " = " + methodName + "(" + valueName + ")");
        }

    }

    public static Tuple<String, String> strSingleValueTemplate(String methodName, String strColumn, String valueName) {
        String name = methodName + "_" + random();
        if (valueName == null) {
            return new Tuple(name, "def " + name + " = doc['" + strColumn + "'].value." + methodName + "()");
        } else {
            return new Tuple(name, strColumn + "; def " + name + " = " + valueName + "." + methodName + "()");
        }

    }

    public static Tuple<String, String> floor(String strColumn, String valueName) {

        return mathSingleValueTemplate("floor", strColumn, valueName);

    }


    //substring(Column str, int pos, int len)
    public static Tuple<String, String> substring(String strColumn, int pos, int len, String valueName) {
        String name = "substring_" + random();
        if (valueName == null) {
            return new Tuple(name, "def " + name + " = doc['" + strColumn + "'].value.substring(" + pos + "," + len + ")");
        } else {
            return new Tuple(name, strColumn + ";def " + name + " = " + valueName + ".substring(" + pos + "," + len + ")");
        }

    }

    //split(Column str, java.lang.String pattern)
    public static Tuple<String, String> split(String strColumn, String pattern, String valueName) {
        String name = "split_" + random();
        if (valueName == null) {
            return new Tuple(name, "def " + name + " = doc['" + strColumn + "'].value.split('" + pattern + "')");
        } else {
            return new Tuple(name, strColumn + "; def " + name + " = " + valueName + ".split('" + pattern + "')");
        }

    }


}
