package com.neopragma.cobolcheck;

import com.neopragma.cobolcheck.exceptions.CommandLineArgumentException;
import com.neopragma.cobolcheck.exceptions.PossibleInternalLogicErrorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Process command line options.
 *
 * @author Dave Nicolette (neopragma)
 * @since 14
 */
public class GetOpt implements Constants, StringHelper {

    private Map<OptionKey, OptionValue> options;
    private static final String LONG_OPT_PREFIX = "--";
    private static final String SHORT_OPT_PREFIX = "-";
    private static final String LONG_OPT_KEYWORD = "--long";
    private static final char ARGUMENT_REQUIRED_INDICATOR = ':';

    /**
     * Parse command-line options using the optionsString to validate.
     *
     * @param args - String[] - options from the command line.
     * @param optionsString - String - Bash-style options specification,
     *                      e.g. "abc:d: --long alpha,bravo,charlie:,delta:"
     */
    public GetOpt(String[] args, String optionsString) {
        if (isEmptyArray(args)) return;
        storeOptionSettings(optionsString);
        processCommandLineArgumentArray(args);
    }

    public String getValueFor(String key) {
        return lookupOption(key).argumentValue;
    }

    public boolean isSet(String key) {
        return lookupOption(key).isSet;
    }

    /**
     * Convert an option specification string into a map of option keys and values.
     *
     * @param optionsString (String) - looks like "a:bc: --long alpha:,beta,charlie:"
     */
    private void storeOptionSettings(String optionsString) {
        if (isBlank(optionsString))
            throw new PossibleInternalLogicErrorException("x");

        options = new HashMap();

        // "a:bg: --long alpha:,beta,gamma:" -> [0] "a:bg:", [1] "--long", [2] "alpha:,beta,gamma:"
        String[] optionSpecs = optionsString.split(SPACE);
        boolean longOptionsWereSpecified = optionSpecs.length > 2 && optionSpecs[1].equals(LONG_OPT_KEYWORD);
        String[] longOptionSpecs = new String[]{ EMPTY_STRING };
        if (longOptionsWereSpecified) longOptionSpecs = optionSpecs[2].split(COMMA);

        String shortOptions = optionSpecs[0];
        int optionOffset = 0;
        int optionIndex = 0;
        while (optionOffset < shortOptions.length()) {
            OptionKey optionKey = new OptionKey();
            optionKey.shortKey = String.valueOf(shortOptions.charAt(optionOffset));
            optionKey.longKey = longOptionsWereSpecified ? longOptionSpecs[optionIndex] : EMPTY_STRING;
            OptionValue optionValue = new OptionValue();
            if (optionOffset+1 < shortOptions.length() && shortOptions.charAt(optionOffset+1) == ARGUMENT_REQUIRED_INDICATOR) {
                optionValue.hasArgument = true;
                optionOffset += 2;
                optionKey.longKey = longOptionsWereSpecified
                        ? optionKey.longKey.substring(0, optionKey.longKey.length()-1)
                        : optionKey.longKey;
            } else {
                optionOffset += 1;
            }
            options.put(optionKey, optionValue);
            optionIndex += 1;
        }

//        System.out.println("Options loaded:");
//        for (OptionKey key : options.keySet()) {
//            System.out.println("Keys: " + key.shortKey + ", " + key.longKey);
//            OptionValue optionValue = lookupOption(key.shortKey);
//            System.out.println("Value: isSet=" + optionValue.isSet
//                + ", hasArgument=" + optionValue.hasArgument
//                + ", argumentValue=" + optionValue.argumentValue);
//        }
    }

    private void processCommandLineArgumentArray(String[] args) {
        boolean expectValueNext = false;
        OptionValue optionValue = new OptionValue();
        for (String argValue : args) {
            if (isKey(argValue)) {
                if (expectValueNext) throw new CommandLineArgumentException("x");
                optionValue = lookupOption(stripPrefix(argValue));
                optionValue.isSet = true;
                expectValueNext = optionValue.hasArgument;
            } else {
                if (!expectValueNext) throw new CommandLineArgumentException("x");
                optionValue.argumentValue = argValue;
                expectValueNext = false;
            }
        }
    }

    private OptionValue lookupOption(String requestedOption) {
        for (OptionKey optionKey : options.keySet()) {
            if (optionKey.shortKey.equals(requestedOption) || optionKey.longKey.equals(requestedOption)) {
                return options.get(optionKey);
            }
        }
        return null; //TODO improve this
    }

    private boolean isKey(String argValue) {
        return argValue.startsWith(SHORT_OPT_PREFIX) || argValue.startsWith(LONG_OPT_PREFIX);
    }

    private String stripPrefix(String argValue) {
        int offset = argValue.startsWith(LONG_OPT_PREFIX) ? 2 : 1;
        return argValue.substring(offset);
    }

    class OptionKey {
        public String shortKey = EMPTY_STRING;
        public String longKey = EMPTY_STRING;
    }

    class OptionValue {
        public boolean hasArgument = false;
        public boolean isSet = false;
        public String argumentValue = EMPTY_STRING;
    }

}