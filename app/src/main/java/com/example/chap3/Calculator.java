package com.example.chap3;

import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class Calculator extends AppCompatActivity implements View.OnClickListener {

    private GestureDetector gestureDetector;
    private TextView tvRes;
    private String firstNum = "";
    private String secondNum = "";
    private String operator = "";
    private String ans = "";
    private String displayText = "";
    private String lastSecondNum = "";
    private String lastOperator = "";
    private Button btn_clr;
    private SoundPool soundPool;
    private int soundId;
    private Button currentToggledButton = null;
    private boolean useNeg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        tvRes = findViewById(R.id.tv_res);
        btn_clr = findViewById(R.id.btn_clear);
        gestureDetector = new GestureDetector(this, new GestureListener());

        tvRes.setOnTouchListener((v, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                v.performClick();
                return true;
            }
            return false;
        });

        soundPool = new SoundPool(1, // 同时播放的最大音效数量
                AudioManager.STREAM_MUSIC, // 音频流类型
                0); // 音质，通常设为0即可
        soundId = soundPool.load(this, R.raw.click_sound, 1);

        findViewById(R.id.btn_clear).setOnClickListener(this);
        findViewById(R.id.btn_neg).setOnClickListener(this);
        findViewById(R.id.btn_mod).setOnClickListener(this);
        findViewById(R.id.btn_divide).setOnClickListener(this);
        findViewById(R.id.btn_seven).setOnClickListener(this);
        findViewById(R.id.btn_eight).setOnClickListener(this);
        findViewById(R.id.btn_nine).setOnClickListener(this);
        findViewById(R.id.btn_multipy).setOnClickListener(this);
        findViewById(R.id.btn_four).setOnClickListener(this);
        findViewById(R.id.btn_five).setOnClickListener(this);
        findViewById(R.id.btn_six).setOnClickListener(this);
        findViewById(R.id.btn_minus).setOnClickListener(this);
        findViewById(R.id.btn_one).setOnClickListener(this);
        findViewById(R.id.btn_two).setOnClickListener(this);
        findViewById(R.id.btn_three).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
        findViewById(R.id.btn_zero).setOnClickListener(this);
        findViewById(R.id.btn_dot).setOnClickListener(this);
        findViewById(R.id.btn_equal).setOnClickListener(this);
    }

    /**
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        String inputText = ((TextView)v).getText().toString();

        // 左右音量、优先级、循环次数、播放速率
        soundPool.play(soundId, 1, 1, 0, 0, 1);

        // AC and C
        if(v.getId() == R.id.btn_clear){
            clear();
            btn_clr.setText(getResources().getString(R.string.all_clear));
            btn_clr.setTextSize(27);
            return;
        }
        else {
            if (displayText.equals(getResources().getString(R.string.err))) return;
            btn_clr.setText(getResources().getString(R.string.clear));
            btn_clr.setTextSize(33);
        }

        // processing
        if (v.getId() == R.id.btn_neg){
            if (displayText.equals("0")) {
                useNeg = true;
                refreshText("-0");
            }
            else if (displayText.equals("-0")) {
                useNeg = false;
                refreshText("0");
            }
            else if (firstNum.isEmpty()) {
                // you can let first num to be -0
                firstNum = "0";
                useNeg = true;
                refreshText("-0");
            }
            // you are inputting first num
            else if (operator.isEmpty()) {
                firstNum = stringFormatter(-1 * Double.parseDouble(firstNum));
                refreshText(firstNum);
            }
            // you are inputting second num
            else {
                if (secondNum.isEmpty()) {
                    secondNum = "0";
                    useNeg = true;
                    refreshText("-0");
                }
                else {
                    secondNum = stringFormatter(-1 * Double.parseDouble(secondNum));
                    refreshText(secondNum);
                }
            }
        }
        else if(v.getId() == R.id.btn_mod){
            if (firstNum.isEmpty()) firstNum = "0";

            if (operator.isEmpty()){
                firstNum = stringFormatter(0.01 * Double.parseDouble(firstNum));
                refreshText(firstNum);
            }
            else {
                if (secondNum.isEmpty())
                    secondNum = stringFormatter(0.01 * Double.parseDouble(firstNum) * Double.parseDouble(firstNum));
                else
                    secondNum = stringFormatter(0.01 * Double.parseDouble(secondNum));

                refreshText(secondNum);
            }
        }
        // operators
        else if (v.getId() == R.id.btn_add || v.getId() == R.id.btn_minus || v.getId() == R.id.btn_multipy || v.getId() == R.id.btn_divide) {

            toggleButtonColor((Button)v);

            if (!firstNum.isEmpty() && !secondNum.isEmpty() && !operator.isEmpty()) {
                if (operator.equals("÷") && secondNum.equals("0")) {
                    whenErrorDo();
                }
                else {
                    double cal_res = calculate();

                    if (cal_res > 1e160 || cal_res < -1e160) {
                        whenErrorDo();
                        return;
                    }

                    refreshOperate(stringFormatter(cal_res));
                    refreshText(ans);
                    operator = inputText;
                }
            }
            else {
                if(firstNum.isEmpty()) firstNum = "0";
                operator = inputText;
            }
        }
        // equals
        else if (v.getId() == R.id.btn_equal) {

            resetButtonColor();

            if (secondNum.isEmpty() && operator.isEmpty()) {
                secondNum = lastSecondNum;
                operator = lastOperator;
            }
            else if (secondNum.isEmpty())
                secondNum = firstNum;

            // num = num
            if (!operator.isEmpty()){
                // divided by zero
                if (operator.equals("÷") && secondNum.equals("0"))
                    whenErrorDo();
                else {
                    double cal_res = calculate();

                    // remember these first so they don't get reset
                    lastSecondNum = secondNum;
                    lastOperator = operator;

                    if(cal_res > 1e160 || cal_res < -1e160){
                        refreshText(getResources().getString(R.string.err));
                        refreshOperate("");
                        return;
                    }

                    refreshOperate(stringFormatter(cal_res));
                    refreshText(ans);
                }
            }
        }
        // numbers and dot
        else{
            resetButtonColor();

            // preventing multiple dots
            if(inputText.equals(".") && displayText.contains(".")) return;

            // after printing the ans and u wanna input a num or smth & theres an "Error"
            if((!ans.isEmpty() && operator.isEmpty()) || displayText.equals("Error")) clear();

            // append the number to the num
            if(operator.isEmpty()) {
                int len = displayText.replace(",", "").replace("-", "").length();
                if (len < 9 || (len < 10 && displayText.contains("."))) {
                    if (useNeg) {
                        firstNum = "-" + firstNum + inputText;
                        useNeg = false;
                    }
                    else firstNum += inputText;
                }
                else return;
            }
            else {
                // when inputting second num i hope this can be cleared
                if (secondNum.isEmpty()) refreshText("");

                int len = displayText.replace(",", "").replace("-", "").length();
                if (len < 9 || (len < 10 && displayText.contains("."))) {
                    if (useNeg) {
                        secondNum = "-" + secondNum + inputText;
                        useNeg = false;
                    }
                    else secondNum += inputText;
                }
                else return;
            }

            // simple decimal issue
            if (displayText.equals("0") && !inputText.equals("."))
                refreshText(inputText);
            else if (displayText.equals(("-0")) && !inputText.equals("."))
                refreshText("-" + inputText);
            else {
                displayText = displayText.replace(",", "");
                refreshText(displayText + inputText);
            }
        }
    }

    private double calculate() {
        switch (operator) {
            case "+":
                return Double.parseDouble(firstNum) + Double.parseDouble(secondNum);
            case "-":
                return Double.parseDouble(firstNum) - Double.parseDouble(secondNum);
            case "×":
                return Double.parseDouble(firstNum) * Double.parseDouble(secondNum);
            default:
                return Double.parseDouble(firstNum) / Double.parseDouble(secondNum);
        }
    }

    private void refreshOperate(String new_ans) {
        ans = new_ans;
        firstNum = ans;
        secondNum = "";
        operator = "";
    }

    private void clear() {
        refreshOperate("");
        refreshText("0");

        // clear these only when user click CLEAR
        lastOperator = "";
        lastSecondNum = "";

        resetButtonColor();
    }

    private void refreshText(String text) {
        String integerPart = text.contains(".") ? text.split("\\.")[0] : text;
        String decimalPart = "";

        if (text.contains(".")) {
            if (text.split("\\.").length > 1)
                decimalPart = "." + text.split("\\.")[1];
            else
                decimalPart = ".";
        }

        boolean isNeg = text.startsWith("-");
        if (isNeg)
            integerPart = integerPart.substring(1);

        StringBuilder sb = new StringBuilder(integerPart);
        for (int i = sb.length() - 3; i > 0; i -= 3) {
            sb.insert(i, ',');
        }

        if (isNeg)
            sb.insert(0, "-");

        sb.append(decimalPart);

        displayText = sb.toString();
        tvRes.setText(displayText);
    }

    private static boolean isDecimal(double num) { return num % 1 != 0; }

    @NonNull
    private String stringFormatter(double cal_res) {
        String str_res;

        // 处理大于或等于 1e9 的数，使用科学计数法
        if (cal_res >= 1e9 || cal_res <= -1e9 || (cal_res >= -1e-8 && cal_res <= 1e-8 && cal_res != 0)) {
            // 科学计数法
            DecimalFormat scientificFormat = new DecimalFormat("0.######E0");
            str_res = scientificFormat.format(cal_res);

            // 检查科学计数法中E后面的指数长度
            int eIndex = str_res.indexOf('E');
            String exponentPart = str_res.substring(eIndex); // 提取指数部分
            String significandPart = str_res.substring(0, eIndex); // 提取有效数字部分

            // 总长度限制9位，减去 'E' 和指数部分的长度
            int availableDigits = 9 - exponentPart.length();

            // 如果有效数字部分超过限制，动态调整小数位数
            if (significandPart.length() > availableDigits) {
                // 动态生成新的格式，保留足够位数
                StringBuilder formatPattern = new StringBuilder("0.");
                for (int i = 0; i < availableDigits - 1; i++) { // 预留1位给整数部分
                    formatPattern.append("#");
                }
                scientificFormat = new DecimalFormat(formatPattern + "E0");
                str_res = scientificFormat.format(cal_res);
            }
        }
        else {
            // 小于 1e9 的数，动态调整小数位数以保证最多 9 位数字
            String[] parts = String.valueOf(cal_res).split("\\.");
            int integerLength = parts[0].length();  // 整数部分的长度

            // 计算可以保留的小数位数，确保总共不超过 9 位
            int maxDecimalPlaces = Math.max(0, 9 - integerLength);

            // 动态生成格式字符串（去掉尾部多余的 0）
            StringBuilder formatPattern = new StringBuilder("0");
            if (maxDecimalPlaces > 0 && isDecimal(cal_res)) {
                formatPattern.append(".");
                for (int i = 0; i < maxDecimalPlaces; i++) {
                    formatPattern.append("#");  // 使用 '#' 来去掉后导 0
                }
            }

            // 使用DecimalFormat按照动态生成的格式保留相应的位数
            DecimalFormat decimalFormat = new DecimalFormat(formatPattern.toString());
            str_res = decimalFormat.format(cal_res);
        }

        // 将 -0 转换为 0
        if (str_res.equals("-0")) return "0";

        return str_res;
    }

    private void whenErrorDo() {
        refreshText(getResources().getString(R.string.err));
        refreshOperate("");
    }

    private void toggleButtonColor(Button btn) {
        final int toggledColor = getResources().getColor(R.color.white);
        final int defaultColor = getResources().getColor(R.color.calc_orange);

        if (currentToggledButton != null && currentToggledButton != btn) {
            currentToggledButton.setBackgroundColor(defaultColor);
            currentToggledButton.setTextColor(toggledColor);
            changeRippleColor(currentToggledButton, getResources().getColor(R.color.ripplewhite));
        }

        if (currentToggledButton != btn) {
            btn.setBackgroundColor(toggledColor);
            btn.setTextColor(defaultColor);
            changeRippleColor(btn, getResources().getColor(R.color.calc_orange));
            currentToggledButton = btn;
        }
    }

    private void resetButtonColor() {
        final int toggledColor = getResources().getColor(R.color.white);
        final int defaultColor = getResources().getColor(R.color.calc_orange);

        if (currentToggledButton != null) {
            currentToggledButton.setBackgroundColor(defaultColor);
            currentToggledButton.setTextColor(toggledColor);
            changeRippleColor(currentToggledButton, getResources().getColor(R.color.ripplewhite));
            currentToggledButton = null;
        }
    }

    private void changeRippleColor(View view, int color) {
        if (view.getBackground() instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) view.getBackground();
            rippleDrawable.setColor(ColorStateList.valueOf(color));
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                deleteCharacter();
                return true;
            }
            return false;
        }
    }

    private void deleteCharacter() {
        if (!ans.isEmpty()) return;

        if (operator.isEmpty())
            firstNum = pop_back(firstNum);
        else
            secondNum = pop_back(secondNum);

        displayText = displayText.replace(",", "");
        refreshText(pop_back(displayText));
    }

    private String pop_back(String s) {
        if (s.length() == 2) {
            if (s.equals("-0")) {
                useNeg = false;
                return "0";
            }
            else if (s.contains("-"))
                return "-0";
            else
                return s.substring(0, s.length() - 1);
        }
        else if (s.length() > 2)
            return s.substring(0, s.length() - 1);
        else
            return "0";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

}
