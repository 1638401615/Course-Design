package hust.cs.javacourse.search.parse.impl;

import hust.cs.javacourse.search.index.AbstractTermTuple;
import hust.cs.javacourse.search.parse.AbstractTermTupleFilter;
import hust.cs.javacourse.search.parse.AbstractTermTupleStream;
import hust.cs.javacourse.search.util.Config;

public class LengthTermTupleFilter extends AbstractTermTupleFilter {
    /**
     * 构造函数
     *
     * @param input ：Filter的输入，类型为AbstractTermTupleStream
     */
    public LengthTermTupleFilter(AbstractTermTupleStream input) {
        super(input);
    }

    /**
     * 获得下一个三元组
     *
     * @return :下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        AbstractTermTuple termTuple = input.next();//此处可以直接使用next是因为后面放入的input都是scanner修饰过的，next已经被覆盖可以正确使用
        if(termTuple == null)
            return null;
        while (termTuple.term.getContent().length() > Config.TERM_FILTER_MAXLENGTH || termTuple.term.getContent().toString().length() < Config.TERM_FILTER_MINLENGTH){
            termTuple = input.next();
            if(termTuple == null)
                return null;
        }
        return termTuple;
    }
}
