package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.internal.AbstractFunction;
import org.cafebabepy.runtime.object.iterator.PyGeneratorObject;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/30.
 */
class PyInterpretFunctionObject extends AbstractFunction {
    private PyObject body;

    PyInterpretFunctionObject(Python runtime, PyObject context, String name, PyObject arguments, PyObject body) {
        super(runtime, context, name, arguments);

        this.body = body;
    }

    @Override
    protected PyObject evalDefaultValue(PyObject context, PyObject defaultValue) {
        return this.runtime.getEvaluator().eval(context, defaultValue);
    }

    @Override
    protected PyObject callImpl(PyObject context) {
        PyObject asyncBool = getFrame().getNotAppearLocals().get("_async");
        if (asyncBool == null) {
            throw new CafeBabePyException("_async is not found");
        }

        boolean async = asyncBool.isTrue();
        if (async) {
            // FIXME stub
            return this.runtime.newPyObject("coroutine", false);
        }

        YieldSearcher yieldSearcher = new YieldSearcher(this.runtime);

        List<PyObject> yields = yieldSearcher.get(this.body);
        if (!yields.isEmpty()) {
            Yielder<PyObject> yielder = new Yielder<PyObject>() {
                @Override
                public void run() {
                    runtime.getEvaluator().eval(context, body);
                }
            };

            int yieldCount = yields.size();
            for (int i = 0; i < yieldCount; i++) {
                this.runtime.getEvaluator().yielderMap.put(yields.get(i), yielder);
            }

            Yielder.YielderIterable<PyObject> iterable = Yielder.newIterable(yielder);
            Iterator<PyObject> iter = iterable.iterator();

            return new PyGeneratorObject(this.runtime, s -> {
                if (iter.hasNext()) {
                    return iter.next();
                }

                try {
                    Optional<RuntimeException> exceptionOpt = iterable.thrownException();
                    if (exceptionOpt.isPresent()) {
                        RuntimeException e = exceptionOpt.get();
                        if (e instanceof InterpretReturn) {
                            s.stop(runtime, ((InterpretReturn) e).value);
                            return this.runtime.None();

                        } else {
                            throw e;
                        }
                    }
                    s.stop(runtime);
                    return runtime.None();

                } finally {
                    for (int i = 0; i < yieldCount; i++) {
                        this.runtime.getEvaluator().yielderMap.remove(yields.get(i));
                    }
                }
            });
        }

        try {
            return this.runtime.getEvaluator().eval(context, this.body);

        } catch (InterpretReturn e) {
            return e.value;
        }
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("function", false);
    }

    @Override
    public boolean isFromClass() {
        return false;
    }
}