package com.clearwind.Calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.math.*
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {

    private lateinit var tvResult: TextView
    private var inputExpression = ""
    private var lastWasOperator = false
    private var currentNumberHasDecimal = false
    private var isScientificMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tv_result)

        // 基础按钮列表
        val basicButtons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
            R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
            R.id.btn_8, R.id.btn_9, R.id.btn_add, R.id.btn_sub,
            R.id.btn_mul, R.id.btn_div, R.id.btn_equal, R.id.btn_percent
        )

        // 科学计算器按钮列表
//        val scientificButtons = listOf(
//            R.id.btn_sin, R.id.btn_cos, R.id.btn_tan,
//            R.id.btn_sqrt, R.id.btn_power, R.id.btn_pi,
//            R.id.btn_log, R.id.btn_ln, R.id.btn_factorial,
//            R.id.btn_left_bracket, R.id.btn_right_bracket,
//            R.id.btn_e
//        )

        // 设置基础按钮点击事件
        for (id in basicButtons) {
            findViewById<Button>(id)?.setOnClickListener {
                handleBasicButtonClick(it as Button)
            }
        }

        // 设置科学计算器按钮点击事件
//        for (id in scientificButtons) {
//            findViewById<Button>(id)?.setOnClickListener {
//                handleScientificButtonClick(it as Button)
//            }
//        }

        // 小数点按钮
        findViewById<Button>(R.id.btn_dot)?.setOnClickListener {
            if (!currentNumberHasDecimal && !lastWasOperator) {
                inputExpression += "."
                currentNumberHasDecimal = true
                lastWasOperator = false
                tvResult.text = inputExpression
            }
        }

        // 退格按钮
        findViewById<Button>(R.id.btn_backspace)?.setOnClickListener {
            if (inputExpression.isNotEmpty()) {
                val lastChar = inputExpression.last()
                if (lastChar == '.') {
                    currentNumberHasDecimal = false
                }
                inputExpression = inputExpression.substring(0, inputExpression.length - 1)
                lastWasOperator = inputExpression.isNotEmpty() &&
                        inputExpression.last() in listOf('+', '-', '*', '/', '%')
                tvResult.text = if (inputExpression.isEmpty()) "0" else inputExpression
            }
        }

        // 清空按钮
        findViewById<Button>(R.id.btn_clear)?.setOnClickListener {
            inputExpression = ""
            lastWasOperator = false
            currentNumberHasDecimal = false
            tvResult.text = "0"
        }

        // 计算结果按钮
        findViewById<Button>(R.id.btn_equal)?.setOnClickListener {
            if (inputExpression.isEmpty()) {
                showError("请输入表达式")
                return@setOnClickListener
            }

            if (lastWasOperator) {
                showError("表达式不能以运算符结尾")
                return@setOnClickListener
            }

            // 检查连续运算符
            var hasConsecutiveOperators = false
            for (i in 1 until inputExpression.length) {
                if (inputExpression[i] in "+-*/%" && inputExpression[i-1] in "+-*/%") {
                    hasConsecutiveOperators = true
                    break
                }
            }
            if (hasConsecutiveOperators) {
                showError("不能输入连续运算符")
                return@setOnClickListener
            }

            // 检查非法字符
            if (!inputExpression.matches(Regex("[0-9+\\-*/%.()]+"))) {
                showError("包含非法字符")
                return@setOnClickListener
            }

            // 检查表达式是否以小数点开头
            if (inputExpression.startsWith(".")) {
                inputExpression = "0$inputExpression"
            }

            // 检查表达式是否以小数点结尾
            if (inputExpression.endsWith(".")) {
                inputExpression += "0"
            }

            // 检查表达式是否包含连续的小数点
            if (inputExpression.contains("..")) {
                showError("表达式格式错误")
                return@setOnClickListener
            }

            val result = evaluateExpression(inputExpression)
            if (result == Double.POSITIVE_INFINITY || result == Double.NEGATIVE_INFINITY) {
                showError("除数不能为零")
                return@setOnClickListener
            }
            if (result.isNaN()) {
                return@setOnClickListener
            }

            // 格式化结果显示
            val formattedResult = if (result % 1 == 0.0) {
                result.toLong().toString()
            } else {
                result.toString()
            }
            tvResult.text = formattedResult
            inputExpression = formattedResult
            lastWasOperator = false
            currentNumberHasDecimal = formattedResult.contains(".")
        }
    }

    private fun handleBasicButtonClick(button: Button) {
        val buttonText = button.text.toString()
        val operatorText = when (buttonText) {
            "×" -> "*"
            "÷" -> "/"
            else -> buttonText
        }
        val isOperator = operatorText in listOf("+", "-", "*", "/", "%")

        if (inputExpression.isEmpty() && isOperator) {
            inputExpression = "0$operatorText"
        } else {
            inputExpression += operatorText
        }
        lastWasOperator = isOperator
        if (isOperator) {
            currentNumberHasDecimal = false
        }
        tvResult.text = inputExpression
    }

    private fun handleScientificButtonClick(button: Button) {
        val buttonText = button.text.toString()
        when (buttonText) {
            "sin" -> {
                inputExpression += "sin("
                lastWasOperator = true
            }
            "cos" -> {
                inputExpression += "cos("
                lastWasOperator = true
            }
            "tan" -> {
                inputExpression += "tan("
                lastWasOperator = true
            }
            "√" -> {
                inputExpression += "sqrt("
                lastWasOperator = true
            }
            "^" -> {
                inputExpression += "^"
                lastWasOperator = true
            }
            "π" -> {
                inputExpression += PI.toString()
                lastWasOperator = false
            }
            "log" -> {
                inputExpression += "log("
                lastWasOperator = true
            }
            "ln" -> {
                inputExpression += "ln("
                lastWasOperator = true
            }
            "n!" -> {
                inputExpression += "fact("
                lastWasOperator = true
            }
            "(" -> {
                inputExpression += "("
                lastWasOperator = true
            }
            ")" -> {
                inputExpression += ")"
                lastWasOperator = false
            }
            "e" -> {
                inputExpression += E.toString()
                lastWasOperator = false
            }
        }
        tvResult.text = inputExpression
    }

    private fun evaluateExpression(expression: String): Double {
        if (expression.isEmpty()) return 0.0

        var currentNumber = StringBuilder()
        var numbers = mutableListOf<BigDecimal>()
        var operators = mutableListOf<Char>()
        var i = 0

        while (i < expression.length) {
            val char = expression[i]
            when (char) {
                '+', '-', '*', '/', '%', '^', '(', ')' -> {
                    if (currentNumber.isNotEmpty()) {
                        numbers.add(BigDecimal(currentNumber.toString()))
                        currentNumber.clear()
                    }
                    operators.add(char)
                    i++
                }
                '.' -> {
                    if (currentNumber.contains('.')) {
                        showError("非法小数格式")
                        return Double.NaN
                    }
                    currentNumber.append(char)
                    i++
                }
                'e' -> {
                    // 检查e后面是否直接跟数字
                    if (i + 1 < expression.length && expression[i + 1].isDigit()) {
                        // 处理科学计数法
                        var j = i + 1
                        while (j < expression.length && expression[j].isDigit()) {
                            j++
                        }
                        val exponent = expression.substring(i + 1, j).toDouble()
                        val base = if (currentNumber.isNotEmpty()) {
                            val number = BigDecimal(currentNumber.toString())
                            currentNumber.clear()
                            number
                        } else {
                            BigDecimal(1.0)
                        }
                        numbers.add(base.multiply(BigDecimal(10.0).pow(exponent.toInt())))
                        i = j
                    } else {
                        // 处理自然常数e
                        numbers.add(BigDecimal(E.toString()))
                        i++
                    }
                }
                'π' -> {
                    // 检查π前面是否有数字
                    if (currentNumber.isNotEmpty()) {
                        val number = BigDecimal(currentNumber.toString())
                        currentNumber.clear()
                        numbers.add(number.multiply(BigDecimal(PI.toString())))
                    } else {
                        numbers.add(BigDecimal(PI.toString()))
                    }
                    i++
                }
                else -> {
                    currentNumber.append(char)
                    i++
                }
            }
        }

        if (currentNumber.isNotEmpty()) {
            numbers.add(BigDecimal(currentNumber.toString()))
        }

        // 处理运算符
        var result = numbers[0]
        for (j in 1 until numbers.size) {
            when (operators[j - 1]) {
                '+' -> result = result.add(numbers[j])
                '-' -> result = result.subtract(numbers[j])
                '*' -> result = result.multiply(numbers[j])
                '/' -> {
                    if (numbers[j] == BigDecimal.ZERO) {
                        showError("除数不能为零")
                        return Double.NaN
                    }
                    result = result.divide(numbers[j], 10, RoundingMode.HALF_UP)
                }
                '%' -> {
                    if (numbers[j] == BigDecimal.ZERO) {
                        showError("除数不能为零")
                        return Double.NaN
                    }
                    result = result.remainder(numbers[j])
                }
                '^' -> result = result.pow(numbers[j].toInt())
            }
        }

        return result.toDouble()
    }

    private fun factorial(n: Double): Double {
        if (n % 1 != 0.0 || n < 0) {
            showError("阶乘运算必须使用非负整数")
            return Double.NaN
        }
        var result = 1.0
        for (i in 1..n.toInt()) {
            result *= i
        }
        return result
    }

    private fun showError(message: String) {
        tvResult.text = "错误: $message"
        inputExpression = ""
        lastWasOperator = false
        currentNumberHasDecimal = false
    }
}
