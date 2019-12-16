package com.breiter.chatter.tool;

import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    public static boolean isEmailValid(String emailInput) {
        Pattern emailAddress = Patterns.EMAIL_ADDRESS;
        Matcher isEmail = emailAddress.matcher(emailInput);
        return isEmail.find();
    }

    public static boolean isPasswordValid(String passwordInput) {
        if (passwordInput.length() >= 8) {

            Pattern letter = Pattern.compile("[a-zA-Z]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile("\\p{Punct}"); //Special character : !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~

            Matcher hasLetter = letter.matcher(passwordInput);
            Matcher hasDigit = digit.matcher(passwordInput);
            Matcher hasSpecial = special.matcher(passwordInput);

            return hasLetter.find() && hasDigit.find() && hasSpecial.find();

        } else
            return false;

    }


}
