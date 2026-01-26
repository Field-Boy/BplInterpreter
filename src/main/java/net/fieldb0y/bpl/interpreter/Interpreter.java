package net.fieldb0y.bpl.interpreter;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;
import net.fieldb0y.bpl.interpreter.ast.Program;
import net.fieldb0y.bpl.interpreter.ast.node.expression.*;
import net.fieldb0y.bpl.interpreter.ast.node.statement.*;
import net.fieldb0y.bpl.interpreter.environment.Environment;
import net.fieldb0y.bpl.interpreter.environment.function.Callable;
import net.fieldb0y.bpl.interpreter.environment.variable.Variable;
import net.fieldb0y.bpl.interpreter.environment.function.Function;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.exception.helper.BreakException;
import net.fieldb0y.bpl.interpreter.exception.helper.ContinueException;
import net.fieldb0y.bpl.interpreter.exception.helper.ReturnException;
import net.fieldb0y.bpl.interpreter.object.*;
import net.fieldb0y.bpl.interpreter.object.number.*;
import net.fieldb0y.bpl.interpreter.utils.ArrayUtils;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static net.fieldb0y.bpl.interpreter.Token.Type.*;

public class Interpreter implements AstVisitor<TypedObject<?>> {
    private Environment env;
    private int loopDepth = 0;

    public Interpreter(Callable... builtinFunctions){
        this.env = new Environment();
        for (Callable func : builtinFunctions){
            env.defineFunction(func);
        }
    }

    public TypedObject<?> evaluate(Expression expr){
        return expr != null ? expr.visit(this) : null;
    }

    public void execute(Statement stmt){
        if (stmt != null)
            stmt.visit(this);
    }

    @Override
    public void visitProgram(Program program) {
        try {
            for (Statement stmt : program.statements()){
                if (stmt instanceof FunctionDeclaration decl)
                    visitFunctionDeclaration(decl);
            }

            for (Statement stmt : program.statements()){
                if (!(stmt instanceof FunctionDeclaration))
                    execute(stmt);
            }
        } catch (RuntimeError e){
            System.err.println("Runtime Error: " + e.getMessage());
        } catch (ReturnException e){
            System.err.println("Runtime Error: 'return' outside function");
        } catch (BreakException e){
            System.err.println("Runtime Error: 'break' outside loop");
        } catch (ContinueException e){
            System.err.println("Runtime Error: 'continue' outside loop");
        }
    }

    @Override
    public void visitVariableDeclaration(VariableDeclaration decl) {
        TypedObject<?> value = BplUtils.getDefaultValue(decl.type());
        Expression initializer = decl.initializer();

        if (initializer != null) {
            TypedObject<?> newVal = evaluate(initializer);
            if (value.isAssignable(newVal)){
                value = BplUtils.getTypedObject(decl.type(), newVal);
            } else
                throw new RuntimeError("Cannot assign '" + StringObject.stringify(newVal) + "' to " + decl.type() + " variable '" + decl.name() + "'");
        }
        env.defineVar(decl.name(), new Variable<>(value, decl.isConst()));
    }

    @Override
    public void visitArrayVariableDeclaration(ArrayVariableDeclaration decl) {
        List<Integer> dimensions = calcArrayDimensions(decl.dimensions());
        ArrayObject array;

        if (decl.initializer() != null){
            TypedObject<?> initializer = evaluate(decl.initializer());
            if (initializer instanceof ArrayUtils.ArrayLiteralResult literal){
                List<Integer> literalDimensions = ArrayUtils.inferDimensions(literal);

                if (dimensions.stream().noneMatch(Objects::isNull)){
                    if (!dimensions.equals(literalDimensions))
                        throw new RuntimeError("Array initializer dimensions mismatch for variable '" + decl.name() + "'. Expected "
                                + ArrayUtils.formatDimensions(dimensions) + ", but got " + ArrayUtils.formatDimensions(literalDimensions));
                } else if(dimensions.size() != literalDimensions.size())
                    throw new RuntimeError("Array initializer dimensions mismatch for variable '" + decl.name() + "'. Expected "
                                + ArrayUtils.formatDimensionsWithoutSize(dimensions) + ", but got " + ArrayUtils.formatDimensionsWithoutSize(literalDimensions));
                array = ArrayUtils.literalToArray(decl.type(), literal);
            } else
                throw new RuntimeError("Initializer of array variable '" + decl.name() + "' should be array literal");
        } else array = new ArrayObject(decl.type(), dimensions);

        env.defineVar(decl.name(), new Variable<>(array, decl.isConst()));
    }

    private List<Integer> calcArrayDimensions(List<Expression> dimExpressions){
        List<Integer> dimensions = new ArrayList<>();
        for (Expression expr : dimExpressions){
            if (expr != null) {
                TypedObject<?> val = evaluate(expr);
                if (!BplUtils.canAssign(INT_TYPE, val))
                    throw new RuntimeError("Array dimension size should be INT_TYPE, but got " + BplUtils.toTokenType(val));
                dimensions.add(((Number) BplUtils.getTypedObject(INT_TYPE, val).get()).intValue());
            } else dimensions.add(null);
        }
        return dimensions;
    }

    @Override
    public void visitFunctionDeclaration(FunctionDeclaration decl) {
        env.defineFunction(new Function(decl, env));
    }

    @Override
    public void visitIfStatement(IfStatement stmt) {
        TypedObject<?> condition = evaluate(stmt.condition());
        if (condition instanceof BoolObject b){
            if (b.get())
                execute(stmt.thenBranch());
            else if(stmt.elseBranch() != null)
                execute(stmt.elseBranch());
        }else throw new RuntimeError("Condition in 'if' statement must be boolean");
    }

    @Override
    public void visitForStatement(ForStatement stmt) {
        createEnvironmentAndExecute(environment -> {
            int prevLoopDepth = loopDepth;
            loopDepth++;

            try {
                execute(stmt.initializer());
                while (evaluate(stmt.condition()) instanceof BoolObject b && b.get()) {
                    try {
                        execute(stmt.body());
                    } catch (ContinueException e) {
                        if (!isCurrentLoop()) throw e;
                    } catch (BreakException e) {
                        if (isCurrentLoop()) break;
                        else throw e;
                    }
                    evaluate(stmt.increment());
                }
            } finally {
                loopDepth = prevLoopDepth;
            }
        }, new Environment(env));
    }

    @Override
    public void visitWhileStatement(WhileStatement stmt) {
        createEnvironmentAndExecute(environment -> {
            int prevLoopDepth = loopDepth;
            loopDepth++;

            try {
                while (evaluate(stmt.condition()) instanceof BoolObject b && b.get()) {
                    try {
                        execute(stmt.body());
                    } catch (ContinueException e) {
                        if (!isCurrentLoop()) throw e;
                    } catch (BreakException e) {
                        if (isCurrentLoop()) break;
                        else throw e;
                    }
                }
            } finally {
                loopDepth = prevLoopDepth;
            }
        }, new Environment(env));
    }

    private boolean isCurrentLoop(){
        return loopDepth > 0;
    }

    @Override
    public void visitBlockStatement(BlockStatement block) {
        executeBlock(block, new Environment(env));
    }

    public void executeBlock(BlockStatement block, Environment blockEnv){
        createEnvironmentAndExecute(e -> {
            for(Statement stmt : block.statements()){
                execute(stmt);
            }
        }, blockEnv);
    }

    private void createEnvironmentAndExecute(Consumer<Environment> consumer, Environment blockEnv){
        Environment previous = env;
        env = blockEnv;
        try {
            consumer.accept(blockEnv);
        } finally {
            env = previous;
        }
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement stmt) {
        if (stmt.expression() != null)
            evaluate(stmt.expression());
    }

    @Override
    public void visitReturnStatement(ReturnStatement stmt) {
        TypedObject<?> value = null;
        if (stmt.value() != null)
            value = evaluate(stmt.value());
        throw new ReturnException(value);
    }

    @Override
    public void visitBreakStatement(BreakStatement stmt) {
        throw new BreakException();
    }

    @Override
    public void visitContinueStatement(ContinueStatement stmt) {
        throw new ContinueException();
    }

    @Override
    public TypedObject<?> visitFunctionCall(FunctionCall call) {
        if (!(call.callee() instanceof IdentifierExpression id))
            throw new RuntimeError("Can only call functions by name");

        List<TypedObject<?>> args = new ArrayList<>();
        for (Expression argExpr : call.args()){
            args.add(evaluate(argExpr));
        }
        Callable function = env.getFunction(id.name(), args);
        return function.call(this, args);
    }

    @Override
    public TypedObject<?> visitBinaryExpression(BinaryExpression expr) {
        TypedObject<?> leftObj = evaluate(expr.left());
        TypedObject<?> rightObj = evaluate(expr.right());

        Token op = expr.operator();
        Token.Type opType = getNonAssignOperator(op.type());
        boolean shouldAssign = opType == EQUAL || opType == PLUS_PLUS || opType == MINUS_MINUS || (opType != op.type());

        if (opType == EQUAL){
            if (expr.left() instanceof IdentifierExpression id){
                env.setVar(id.name(), rightObj);
                return rightObj;
            } else if(expr.left() instanceof ArrayAccessExpression accessExpr){
                TypedObject<?> obj = evaluate(accessExpr.expression());
                if (!(obj instanceof ArrayObject array))
                    throw new RuntimeError("Cannot access element of non-array object '" + obj.toString() + "'");
                array.setElement(calcArrayIndices(accessExpr.indices()), rightObj);
                return rightObj;
            } else throwBinaryExprError(leftObj, op, rightObj, "", "Can only assign values to variables");
        }

        if (opType != EQUAL_EQUAL && (leftObj != null && leftObj.get() == null || rightObj != null && rightObj.get() == null))
            throwBinaryExprError(leftObj, op, rightObj, "null", "Value of operand cannot be null");

        TypedObject<?> result = null;
        if (leftObj instanceof TypedNumber<?> n1 && rightObj instanceof TypedNumber<?> n2){
            result = switch (opType){
                case PLUS -> TypedNumber.add(n1, n2);
                case MINUS -> TypedNumber.subtract(n1, n2);
                case STAR -> TypedNumber.multiply(n1, n2);
                case SLASH -> TypedNumber.divide(n1, n2);
                case PERCENT -> TypedNumber.modulo(n1, n2);

                case GREATER -> TypedNumber.compare(n1, ">", n2);
                case GREATER_EQUAL -> TypedNumber.compare(n1, ">=", n2);
                case LESS -> TypedNumber.compare(n1, "<", n2);
                case LESS_EQUAL -> TypedNumber.compare(n1, "<=", n2);
                case EQUAL_EQUAL -> TypedNumber.compare(n1, "==", n2);
                case BANG_EQUAL -> TypedNumber.compare(n1, "!=", n2);

                default -> throw new RuntimeError("Invalid operator " + opType + " for binary expression");
            };
        }
        else if (leftObj == null){
            if (opType == MINUS && rightObj instanceof TypedNumber<?> n)
                result = TypedNumber.opposite(n);
            else if(opType == BANG && rightObj instanceof BoolObject b)
                result = BoolObject.not(b);
        }
        else if (rightObj == null){
            if (leftObj instanceof TypedNumber<?> n){
                if (opType == PLUS_PLUS)
                    result = TypedNumber.increment(n);
                else if (opType == MINUS_MINUS)
                    result = TypedNumber.decrement(n);
            }
        }
        else if(opType == EQUAL_EQUAL){
            result = TypedObject.equals(leftObj, rightObj);
        } else if (opType == BANG_EQUAL){
            result = TypedObject.notEquals(leftObj, rightObj);
        }
        else if (leftObj instanceof StringObject || rightObj instanceof StringObject){
            if (opType == PLUS)
                result = StringObject.makeString(leftObj, rightObj);
        }
        else if (leftObj instanceof BoolObject b1 && rightObj instanceof BoolObject b2){
            if (opType == AND) result = BoolObject.and(b1, b2);
            else if(opType == OR) result = BoolObject.or(b1, b2);
        }

        if (result == null)
            throwBinaryExprError(leftObj, op, rightObj, "", "");
        if (shouldAssign){
            if (expr.left() instanceof ArrayAccessExpression accessExpr){
                TypedObject<?> obj = evaluate(accessExpr.expression());
                if (!(obj instanceof ArrayObject array))
                    throw new RuntimeError("Cannot access element of non-array object '" + obj.toString() + "'");
                array.setElement(calcArrayIndices(accessExpr.indices()), result);
            } else if (expr.left() instanceof IdentifierExpression id)
                env.setVar(id.name(), result);
            else throwBinaryExprError(leftObj, op, rightObj, "", "Can only assign values to variables");
        }

        return result;
    }

    private void throwBinaryExprError(TypedObject<?> leftObj, Token op, TypedObject<?> rightObj, String nullReplacement, String postMsg){
        throw new RuntimeError("Cannot handle binary expression '" + (leftObj != null ? leftObj.get() : nullReplacement) + op.lexeme() + (rightObj != null ? rightObj.get() : nullReplacement) + "'. " + postMsg);
    }

    @Override
    public TypedObject<?> visitLiteralExpression(LiteralExpression expr) {
        return BplUtils.getTypedObjectFromLiteral(expr.type(), expr.value());
    }

    @Override
    public TypedObject<?> visitArrayLiteralExpression(ArrayLiteralExpression expr) {
        List<TypedObject<?>> elements = new ArrayList<>();
        for (Expression elementExpr : expr.elements()){
            TypedObject<?> element = evaluate(elementExpr);
            if (elementExpr instanceof ArrayLiteralExpression && !(element instanceof ArrayUtils.ArrayLiteralResult))
                throw new RuntimeError("Expected ArrayLiteralResult after evaluating ArrayLiteralExpression, but got " + element.getClass().getSimpleName());
            elements.add(element);
        }
        return new ArrayUtils.ArrayLiteralResult(elements);
    }

    @Override
    public TypedObject<?> visitArrayAccessExpression(ArrayAccessExpression expr) {
        TypedObject<?> obj = evaluate(expr.expression());

        if (obj instanceof StringObject str){
            return new CharObject(str.get().charAt(calcArrayIndices(expr.indices()).getFirst()));
        }

        if (!(obj instanceof ArrayObject array))
            throw new RuntimeError("Cannot access element of non-array object '" + obj.toString() + "'");
        return array.getElement(calcArrayIndices(expr.indices()));
    }

    private List<Integer> calcArrayIndices(List<Expression> indexExprs){
        List<Integer> indices = new ArrayList<>();
        for (Expression indexExpr : indexExprs){
            TypedObject<?> index = evaluate(indexExpr);
            if (!BplUtils.canAssign(INT_TYPE, index))
                throw new RuntimeError("Expected INT_TYPE index for accessing value of array, but got " + BplUtils.toTokenType(index));
            indices.add(((Number)BplUtils.getTypedObject(INT_TYPE, index).get()).intValue());
        }
        return indices;
    }

    private Token.Type getNonAssignOperator(Token.Type type){
        return switch (type){
            case PLUS_EQUAL -> PLUS;
            case MINUS_EQUAL-> MINUS;
            case STAR_EQUAL -> STAR;
            case SLASH_EQUAL -> SLASH;
            case PERCENT_EQUAL -> PERCENT;
            default -> type;
        };
    }

    @Override
    public TypedObject<?> visitIdentifierExpression(IdentifierExpression expr) {
        return env.getVarValue(expr.name());
    }

    @Override
    public TypedObject<?> visitCastExpression(CastExpression expr) {
        TypedObject<?> objectToCast = evaluate(expr.expression());
        return objectToCast.cast(expr.castType());
    }
}