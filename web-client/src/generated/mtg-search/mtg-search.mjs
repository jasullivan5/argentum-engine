//region block: polyfills
if (typeof Math.imul === 'undefined') {
  Math.imul = function imul(a, b) {
    return (a & 4.29490176E9) * (b & 65535) + (a & 65535) * (b | 0) | 0;
  };
}
if (typeof ArrayBuffer.isView === 'undefined') {
  ArrayBuffer.isView = function (a) {
    return a != null && a.__proto__ != null && a.__proto__.__proto__ === Int8Array.prototype.__proto__;
  };
}
if (typeof Array.prototype.fill === 'undefined') {
  // Polyfill from https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/fill#Polyfill
  Object.defineProperty(Array.prototype, 'fill', {value: function (value) {
    // Steps 1-2.
    if (this == null) {
      throw new TypeError('this is null or not defined');
    }
    var O = Object(this); // Steps 3-5.
    var len = O.length >>> 0; // Steps 6-7.
    var start = arguments[1];
    var relativeStart = start >> 0; // Step 8.
    var k = relativeStart < 0 ? Math.max(len + relativeStart, 0) : Math.min(relativeStart, len); // Steps 9-10.
    var end = arguments[2];
    var relativeEnd = end === undefined ? len : end >> 0; // Step 11.
    var finalValue = relativeEnd < 0 ? Math.max(len + relativeEnd, 0) : Math.min(relativeEnd, len); // Step 12.
    while (k < finalValue) {
      O[k] = value;
      k++;
    }
    ; // Step 13.
    return O;
  }});
}
[Int8Array, Int16Array, Uint16Array, Int32Array, Float32Array, Float64Array].forEach(function (TypedArray) {
  if (typeof TypedArray.prototype.fill === 'undefined') {
    Object.defineProperty(TypedArray.prototype, 'fill', {value: Array.prototype.fill});
  }
});
if (typeof Math.clz32 === 'undefined') {
  Math.clz32 = function (log, LN2) {
    return function (x) {
      var asUint = x >>> 0;
      if (asUint === 0) {
        return 32;
      }
      return 31 - (log(asUint) / LN2 | 0) | 0; // the "| 0" acts like math.floor
    };
  }(Math.log, Math.LN2);
}
if (typeof String.prototype.startsWith === 'undefined') {
  Object.defineProperty(String.prototype, 'startsWith', {value: function (searchString, position) {
    position = position || 0;
    return this.lastIndexOf(searchString, position) === position;
  }});
}
//endregion
//region block: imports
var imul_0 = Math.imul;
var isView = ArrayBuffer.isView;
var clz32 = Math.clz32;
//endregion
//region block: pre-declaration
initMetadataForInterface(CharSequence, 'CharSequence');
initMetadataForInterface(Comparable, 'Comparable');
initMetadataForClass(Number_0, 'Number');
initMetadataForClass(asSequence$$inlined$Sequence$1);
initMetadataForClass(asIterable$$inlined$Iterable$1);
initMetadataForCompanion(Companion);
initMetadataForClass(Char, 'Char', VOID, VOID, [Comparable]);
initMetadataForInterface(Collection, 'Collection');
initMetadataForInterface(KtSet, 'Set', VOID, VOID, [Collection]);
initMetadataForInterface(KtList, 'List', VOID, VOID, [Collection]);
initMetadataForInterface(Entry, 'Entry');
initMetadataForInterface(KtMap, 'Map');
initMetadataForCompanion(Companion_0);
initMetadataForClass(Enum, 'Enum', VOID, VOID, [Comparable]);
initMetadataForCompanion(Companion_1);
initMetadataForClass(Long, 'Long', VOID, Number_0, [Comparable]);
initMetadataForInterface(FunctionAdapter, 'FunctionAdapter');
initMetadataForObject(Digit, 'Digit');
initMetadataForObject(Letter, 'Letter');
initMetadataForInterface(Comparator, 'Comparator');
initMetadataForObject(Unit, 'Unit');
initMetadataForClass(AbstractCollection, 'AbstractCollection', VOID, VOID, [Collection]);
initMetadataForClass(AbstractMutableCollection, 'AbstractMutableCollection', VOID, AbstractCollection, [Collection]);
initMetadataForClass(IteratorImpl, 'IteratorImpl');
initMetadataForClass(AbstractMutableList, 'AbstractMutableList', VOID, AbstractMutableCollection, [KtList, Collection]);
initMetadataForClass(AbstractMap, 'AbstractMap', VOID, VOID, [KtMap]);
initMetadataForClass(AbstractMutableMap, 'AbstractMutableMap', VOID, AbstractMap, [KtMap]);
initMetadataForClass(AbstractMutableSet, 'AbstractMutableSet', VOID, AbstractMutableCollection, [KtSet, Collection]);
initMetadataForCompanion(Companion_2);
initMetadataForClass(ArrayList, 'ArrayList', ArrayList_init_$Create$, AbstractMutableList, [KtList, Collection]);
initMetadataForClass(HashMap, 'HashMap', HashMap_init_$Create$, AbstractMutableMap, [KtMap]);
initMetadataForClass(HashMapValues, 'HashMapValues', VOID, AbstractMutableCollection, [Collection]);
initMetadataForClass(HashMapEntrySetBase, 'HashMapEntrySetBase', VOID, AbstractMutableSet, [KtSet, Collection]);
initMetadataForClass(HashMapEntrySet, 'HashMapEntrySet', VOID, HashMapEntrySetBase);
initMetadataForClass(HashMapValuesDefault$iterator$1);
initMetadataForClass(HashMapValuesDefault, 'HashMapValuesDefault', VOID, AbstractMutableCollection);
initMetadataForClass(HashSet, 'HashSet', HashSet_init_$Create$, AbstractMutableSet, [KtSet, Collection]);
initMetadataForCompanion(Companion_3);
initMetadataForClass(Itr, 'Itr');
initMetadataForClass(KeysItr, 'KeysItr', VOID, Itr);
initMetadataForClass(ValuesItr, 'ValuesItr', VOID, Itr);
initMetadataForClass(EntriesItr, 'EntriesItr', VOID, Itr);
initMetadataForClass(EntryRef, 'EntryRef', VOID, VOID, [Entry]);
function containsAllEntries(m) {
  var tmp$ret$0;
  $l$block_0: {
    // Inline function 'kotlin.collections.all' call
    var tmp;
    if (isInterface(m, Collection)) {
      tmp = m.l();
    } else {
      tmp = false;
    }
    if (tmp) {
      tmp$ret$0 = true;
      break $l$block_0;
    }
    var _iterator__ex2g4s = m.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      // Inline function 'kotlin.js.unsafeCast' call
      // Inline function 'kotlin.js.asDynamic' call
      var entry = element;
      var tmp_0;
      if (!(entry == null) ? isInterface(entry, Entry) : false) {
        tmp_0 = this.v5(entry);
      } else {
        tmp_0 = false;
      }
      if (!tmp_0) {
        tmp$ret$0 = false;
        break $l$block_0;
      }
    }
    tmp$ret$0 = true;
  }
  return tmp$ret$0;
}
initMetadataForInterface(InternalMap, 'InternalMap');
initMetadataForClass(InternalHashMap, 'InternalHashMap', InternalHashMap_init_$Create$, VOID, [InternalMap]);
initMetadataForObject(EmptyHolder, 'EmptyHolder');
initMetadataForClass(LinkedHashMap, 'LinkedHashMap', LinkedHashMap_init_$Create$, HashMap, [KtMap]);
initMetadataForClass(LinkedHashSet, 'LinkedHashSet', LinkedHashSet_init_$Create$, HashSet, [KtSet, Collection]);
initMetadataForClass(Exception, 'Exception', Exception_init_$Create$, Error);
initMetadataForClass(RuntimeException, 'RuntimeException', RuntimeException_init_$Create$, Exception);
initMetadataForClass(UnsupportedOperationException, 'UnsupportedOperationException', UnsupportedOperationException_init_$Create$, RuntimeException);
initMetadataForClass(IllegalArgumentException, 'IllegalArgumentException', IllegalArgumentException_init_$Create$, RuntimeException);
initMetadataForClass(NoSuchElementException, 'NoSuchElementException', NoSuchElementException_init_$Create$, RuntimeException);
initMetadataForClass(IndexOutOfBoundsException, 'IndexOutOfBoundsException', IndexOutOfBoundsException_init_$Create$, RuntimeException);
initMetadataForClass(IllegalStateException, 'IllegalStateException', IllegalStateException_init_$Create$, RuntimeException);
initMetadataForClass(ConcurrentModificationException, 'ConcurrentModificationException', ConcurrentModificationException_init_$Create$, RuntimeException);
initMetadataForClass(ArithmeticException, 'ArithmeticException', ArithmeticException_init_$Create$, RuntimeException);
initMetadataForClass(NumberFormatException, 'NumberFormatException', NumberFormatException_init_$Create$, IllegalArgumentException);
initMetadataForClass(NoWhenBranchMatchedException, 'NoWhenBranchMatchedException', NoWhenBranchMatchedException_init_$Create$, RuntimeException);
initMetadataForClass(NullPointerException, 'NullPointerException', NullPointerException_init_$Create$, RuntimeException);
initMetadataForClass(ClassCastException, 'ClassCastException', ClassCastException_init_$Create$, RuntimeException);
initMetadataForClass(KClassImpl, 'KClassImpl');
initMetadataForClass(PrimitiveKClassImpl, 'PrimitiveKClassImpl', VOID, KClassImpl);
initMetadataForObject(NothingKClassImpl, 'NothingKClassImpl', VOID, KClassImpl);
initMetadataForInterface(KProperty1, 'KProperty1');
initMetadataForClass(StringBuilder, 'StringBuilder', StringBuilder_init_$Create$_0, VOID, [CharSequence]);
initMetadataForCompanion(Companion_4);
initMetadataForClass(Regex, 'Regex');
initMetadataForClass(MatchGroup, 'MatchGroup');
initMetadataForInterface(MatchNamedGroupCollection, 'MatchNamedGroupCollection', VOID, VOID, [Collection]);
initMetadataForClass(findNext$1$groups$1, VOID, VOID, AbstractCollection, [MatchNamedGroupCollection]);
initMetadataForClass(AbstractList, 'AbstractList', VOID, AbstractCollection, [KtList]);
initMetadataForClass(findNext$1$groupValues$1, VOID, VOID, AbstractList);
initMetadataForClass(findNext$1);
initMetadataForClass(sam$kotlin_Comparator$0, 'sam$kotlin_Comparator$0', VOID, VOID, [Comparator, FunctionAdapter]);
initMetadataForClass(IteratorImpl_0, 'IteratorImpl');
initMetadataForCompanion(Companion_5);
initMetadataForClass(AbstractMap$values$1$iterator$1);
initMetadataForCompanion(Companion_6);
initMetadataForClass(AbstractMap$values$1, VOID, VOID, AbstractCollection);
initMetadataForCompanion(Companion_7);
initMetadataForCompanion(Companion_8);
initMetadataForClass(ArrayDeque, 'ArrayDeque', ArrayDeque_init_$Create$, AbstractMutableList);
initMetadataForObject(EmptyList, 'EmptyList', VOID, VOID, [KtList]);
initMetadataForObject(EmptyIterator, 'EmptyIterator');
initMetadataForInterface(MapWithDefault, 'MapWithDefault', VOID, VOID, [KtMap]);
initMetadataForObject(EmptyMap, 'EmptyMap', VOID, VOID, [KtMap]);
initMetadataForClass(IntIterator, 'IntIterator');
initMetadataForInterface(DropTakeSequence, 'DropTakeSequence');
initMetadataForClass(TakeSequence$iterator$1);
initMetadataForClass(TakeSequence, 'TakeSequence', VOID, VOID, [DropTakeSequence]);
initMetadataForClass(TransformingSequence$iterator$1);
initMetadataForClass(TransformingSequence, 'TransformingSequence');
initMetadataForClass(GeneratorSequence$iterator$1);
initMetadataForClass(GeneratorSequence, 'GeneratorSequence');
initMetadataForObject(EmptySequence, 'EmptySequence', VOID, VOID, [DropTakeSequence]);
initMetadataForObject(EmptySet, 'EmptySet', VOID, VOID, [KtSet]);
initMetadataForObject(NaturalOrderComparator, 'NaturalOrderComparator', VOID, VOID, [Comparator]);
initMetadataForCompanion(Companion_9);
initMetadataForClass(IntProgression, 'IntProgression');
initMetadataForClass(IntRange, 'IntRange', VOID, IntProgression);
initMetadataForClass(IntProgressionIterator, 'IntProgressionIterator', VOID, IntIterator);
initMetadataForCompanion(Companion_10);
initMetadataForClass(DelimitedRangesSequence$iterator$1);
initMetadataForClass(DelimitedRangesSequence, 'DelimitedRangesSequence');
initMetadataForClass(UnsafeLazyImpl, 'UnsafeLazyImpl');
initMetadataForObject(UNINITIALIZED_VALUE, 'UNINITIALIZED_VALUE');
initMetadataForClass(Pair, 'Pair');
initMetadataForClass(Span, 'Span');
initMetadataForCompanion(Companion_11);
initMetadataForClass(Op, 'Op', VOID, Enum);
initMetadataForObject(LParen, 'LParen');
initMetadataForObject(RParen, 'RParen');
initMetadataForObject(Or, 'Or');
initMetadataForObject(And, 'And');
initMetadataForObject(Not, 'Not');
initMetadataForObject(Term, 'Term');
initMetadataForClass(Token, 'Token');
initMetadataForClass(AndNode, 'AndNode');
initMetadataForClass(OrNode, 'OrNode');
initMetadataForClass(NotNode, 'NotNode');
initMetadataForClass(AtomNode, 'AtomNode');
initMetadataForClass(ParseError, 'ParseError');
initMetadataForClass(Fix, 'Fix');
initMetadataForObject(Colorless, 'Colorless');
initMetadataForObject(Multi, 'Multi');
initMetadataForObject(Mono, 'Mono');
initMetadataForClass(Colors, 'Colors');
initMetadataForClass(Count, 'Count');
initMetadataForClass(Error_0, 'Error');
initMetadataForObject(ColorSugar, 'ColorSugar');
initMetadataForClass(CompileResult, 'CompileResult');
initMetadataForObject(Evaluator, 'Evaluator');
initMetadataForClass(KeyOpMatch, 'KeyOpMatch');
initMetadataForClass(ParsedValue, 'ParsedValue');
initMetadataForObject(Lexer, 'Lexer');
initMetadataForClass(CostColors, 'CostColors');
initMetadataForObject(ManaCostMatch, 'ManaCostMatch');
initMetadataForClass(Ok, 'Ok');
initMetadataForClass(Err, 'Err');
initMetadataForClass(NumericField, 'NumericField');
initMetadataForClass(Matchers$entry$1);
initMetadataForObject(Matchers, 'Matchers');
initMetadataForClass(ParseAst, 'ParseAst');
initMetadataForClass(State, 'State');
initMetadataForClass(NodeKind, 'NodeKind', VOID, Enum);
initMetadataForObject(Parser, 'Parser');
function get_printingSetCodes() {
  return emptyList();
}
function get_printedNames() {
  return emptyList();
}
function get_loyalty() {
  return null;
}
function get_view() {
  return new SearchView(this);
}
initMetadataForInterface(SearchCard, 'SearchCard');
initMetadataForClass(SearchView, 'SearchView');
initMetadataForCompanion(Companion_12);
initMetadataForClass(SearchQuery, 'SearchQuery');
initMetadataForClass(SearchKey, 'SearchKey');
initMetadataForClass(CompiledQuery, 'CompiledQuery');
initMetadataForClass(QueryError, 'QueryError');
initMetadataForClass(QueryFix, 'QueryFix');
initMetadataForClass(JsSearchCard, 'JsSearchCard', VOID, VOID, [SearchCard]);
//endregion
function CharSequence() {
}
function Comparable() {
}
function Number_0() {
}
function indexOf(_this__u8e3s4, element) {
  if (element == null) {
    var inductionVariable = 0;
    var last = _this__u8e3s4.length - 1 | 0;
    if (inductionVariable <= last)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        if (_this__u8e3s4[index] == null) {
          return index;
        }
      }
       while (inductionVariable <= last);
  } else {
    var inductionVariable_0 = 0;
    var last_0 = _this__u8e3s4.length - 1 | 0;
    if (inductionVariable_0 <= last_0)
      do {
        var index_0 = inductionVariable_0;
        inductionVariable_0 = inductionVariable_0 + 1 | 0;
        if (equals(element, _this__u8e3s4[index_0])) {
          return index_0;
        }
      }
       while (inductionVariable_0 <= last_0);
  }
  return -1;
}
function get_lastIndex(_this__u8e3s4) {
  return _this__u8e3s4.length - 1 | 0;
}
function joinToString(_this__u8e3s4, separator, prefix, postfix, limit, truncated, transform) {
  separator = separator === VOID ? ', ' : separator;
  prefix = prefix === VOID ? '' : prefix;
  postfix = postfix === VOID ? '' : postfix;
  limit = limit === VOID ? -1 : limit;
  truncated = truncated === VOID ? '...' : truncated;
  transform = transform === VOID ? null : transform;
  return joinTo(_this__u8e3s4, StringBuilder_init_$Create$_0(), separator, prefix, postfix, limit, truncated, transform).toString();
}
function joinTo(_this__u8e3s4, buffer, separator, prefix, postfix, limit, truncated, transform) {
  separator = separator === VOID ? ', ' : separator;
  prefix = prefix === VOID ? '' : prefix;
  postfix = postfix === VOID ? '' : postfix;
  limit = limit === VOID ? -1 : limit;
  truncated = truncated === VOID ? '...' : truncated;
  transform = transform === VOID ? null : transform;
  buffer.e(prefix);
  var count = 0;
  var inductionVariable = 0;
  var last = _this__u8e3s4.length;
  $l$loop: while (inductionVariable < last) {
    var element = _this__u8e3s4[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    count = count + 1 | 0;
    if (count > 1) {
      buffer.e(separator);
    }
    if (limit < 0 || count <= limit) {
      appendElement(buffer, element, transform);
    } else
      break $l$loop;
  }
  if (limit >= 0 && count > limit) {
    buffer.e(truncated);
  }
  buffer.e(postfix);
  return buffer;
}
function contains(_this__u8e3s4, element) {
  return indexOf(_this__u8e3s4, element) >= 0;
}
function single(_this__u8e3s4) {
  var tmp;
  switch (_this__u8e3s4.length) {
    case 0:
      throw NoSuchElementException_init_$Create$_0('Array is empty.');
    case 1:
      tmp = _this__u8e3s4[0];
      break;
    default:
      throw IllegalArgumentException_init_$Create$_0('Array has more than one element.');
  }
  return tmp;
}
function toCollection(_this__u8e3s4, destination) {
  var inductionVariable = 0;
  var last = _this__u8e3s4.length;
  while (inductionVariable < last) {
    var item = _this__u8e3s4[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    destination.f(item);
  }
  return destination;
}
function contains_0(_this__u8e3s4, element) {
  return indexOf_0(_this__u8e3s4, element) >= 0;
}
function indexOf_0(_this__u8e3s4, element) {
  var inductionVariable = 0;
  var last = _this__u8e3s4.length - 1 | 0;
  if (inductionVariable <= last)
    do {
      var index = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      if (element === _this__u8e3s4[index]) {
        return index;
      }
    }
     while (inductionVariable <= last);
  return -1;
}
function joinToString_0(_this__u8e3s4, separator, prefix, postfix, limit, truncated, transform) {
  separator = separator === VOID ? ', ' : separator;
  prefix = prefix === VOID ? '' : prefix;
  postfix = postfix === VOID ? '' : postfix;
  limit = limit === VOID ? -1 : limit;
  truncated = truncated === VOID ? '...' : truncated;
  transform = transform === VOID ? null : transform;
  return joinTo_0(_this__u8e3s4, StringBuilder_init_$Create$_0(), separator, prefix, postfix, limit, truncated, transform).toString();
}
function joinTo_0(_this__u8e3s4, buffer, separator, prefix, postfix, limit, truncated, transform) {
  separator = separator === VOID ? ', ' : separator;
  prefix = prefix === VOID ? '' : prefix;
  postfix = postfix === VOID ? '' : postfix;
  limit = limit === VOID ? -1 : limit;
  truncated = truncated === VOID ? '...' : truncated;
  transform = transform === VOID ? null : transform;
  buffer.e(prefix);
  var count = 0;
  var _iterator__ex2g4s = _this__u8e3s4.g();
  $l$loop: while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    count = count + 1 | 0;
    if (count > 1) {
      buffer.e(separator);
    }
    if (limit < 0 || count <= limit) {
      appendElement(buffer, element, transform);
    } else
      break $l$loop;
  }
  if (limit >= 0 && count > limit) {
    buffer.e(truncated);
  }
  buffer.e(postfix);
  return buffer;
}
function toSet(_this__u8e3s4) {
  if (isInterface(_this__u8e3s4, Collection)) {
    var tmp;
    switch (_this__u8e3s4.j()) {
      case 0:
        tmp = emptySet();
        break;
      case 1:
        var tmp_0;
        if (isInterface(_this__u8e3s4, KtList)) {
          tmp_0 = _this__u8e3s4.k(0);
        } else {
          tmp_0 = _this__u8e3s4.g().i();
        }

        tmp = setOf(tmp_0);
        break;
      default:
        tmp = toCollection_0(_this__u8e3s4, LinkedHashSet_init_$Create$_1(mapCapacity(_this__u8e3s4.j())));
        break;
    }
    return tmp;
  }
  return optimizeReadOnlySet(toCollection_0(_this__u8e3s4, LinkedHashSet_init_$Create$()));
}
function sorted(_this__u8e3s4) {
  if (isInterface(_this__u8e3s4, Collection)) {
    if (_this__u8e3s4.j() <= 1)
      return toList(_this__u8e3s4);
    // Inline function 'kotlin.collections.toTypedArray' call
    var tmp = copyToArray(_this__u8e3s4);
    // Inline function 'kotlin.apply' call
    var this_0 = isArray(tmp) ? tmp : THROW_CCE();
    sort(this_0);
    return asList(this_0);
  }
  // Inline function 'kotlin.apply' call
  var this_1 = toMutableList(_this__u8e3s4);
  sort_0(this_1);
  return this_1;
}
function first(_this__u8e3s4) {
  if (_this__u8e3s4.l())
    throw NoSuchElementException_init_$Create$_0('List is empty.');
  return _this__u8e3s4.k(0);
}
function dropLast(_this__u8e3s4, n) {
  // Inline function 'kotlin.require' call
  if (!(n >= 0)) {
    var message = 'Requested element count ' + n + ' is less than zero.';
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  return take(_this__u8e3s4, coerceAtLeast(_this__u8e3s4.j() - n | 0, 0));
}
function last(_this__u8e3s4) {
  if (_this__u8e3s4.l())
    throw NoSuchElementException_init_$Create$_0('List is empty.');
  return _this__u8e3s4.k(get_lastIndex_0(_this__u8e3s4));
}
function distinct(_this__u8e3s4) {
  return toList(toMutableSet(_this__u8e3s4));
}
function plus(_this__u8e3s4, elements) {
  if (isInterface(elements, Collection)) {
    var result = ArrayList_init_$Create$_0(_this__u8e3s4.j() + elements.j() | 0);
    result.p(_this__u8e3s4);
    result.p(elements);
    return result;
  } else {
    var result_0 = ArrayList_init_$Create$_1(_this__u8e3s4);
    addAll(result_0, elements);
    return result_0;
  }
}
function toIntArray(_this__u8e3s4) {
  var result = new Int32Array(_this__u8e3s4.j());
  var index = 0;
  var _iterator__ex2g4s = _this__u8e3s4.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    var _unary__edvuaz = index;
    index = _unary__edvuaz + 1 | 0;
    result[_unary__edvuaz] = element;
  }
  return result;
}
function toCollection_0(_this__u8e3s4, destination) {
  var _iterator__ex2g4s = _this__u8e3s4.g();
  while (_iterator__ex2g4s.h()) {
    var item = _iterator__ex2g4s.i();
    destination.f(item);
  }
  return destination;
}
function toList(_this__u8e3s4) {
  if (isInterface(_this__u8e3s4, Collection)) {
    var tmp;
    switch (_this__u8e3s4.j()) {
      case 0:
        tmp = emptyList();
        break;
      case 1:
        var tmp_0;
        if (isInterface(_this__u8e3s4, KtList)) {
          tmp_0 = _this__u8e3s4.k(0);
        } else {
          tmp_0 = _this__u8e3s4.g().i();
        }

        tmp = listOf(tmp_0);
        break;
      default:
        tmp = toMutableList_0(_this__u8e3s4);
        break;
    }
    return tmp;
  }
  return optimizeReadOnlyList(toMutableList(_this__u8e3s4));
}
function toMutableList(_this__u8e3s4) {
  if (isInterface(_this__u8e3s4, Collection))
    return toMutableList_0(_this__u8e3s4);
  return toCollection_0(_this__u8e3s4, ArrayList_init_$Create$());
}
function take(_this__u8e3s4, n) {
  // Inline function 'kotlin.require' call
  if (!(n >= 0)) {
    var message = 'Requested element count ' + n + ' is less than zero.';
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  if (n === 0)
    return emptyList();
  if (isInterface(_this__u8e3s4, Collection)) {
    if (n >= _this__u8e3s4.j())
      return toList(_this__u8e3s4);
    if (n === 1)
      return listOf(first_0(_this__u8e3s4));
  }
  var count = 0;
  var list = ArrayList_init_$Create$_0(n);
  var _iterator__ex2g4s = _this__u8e3s4.g();
  $l$loop: while (_iterator__ex2g4s.h()) {
    var item = _iterator__ex2g4s.i();
    list.f(item);
    count = count + 1 | 0;
    if (count === n)
      break $l$loop;
  }
  return optimizeReadOnlyList(list);
}
function toMutableSet(_this__u8e3s4) {
  var tmp;
  if (isInterface(_this__u8e3s4, Collection)) {
    tmp = LinkedHashSet_init_$Create$_0(_this__u8e3s4);
  } else {
    tmp = toCollection_0(_this__u8e3s4, LinkedHashSet_init_$Create$());
  }
  return tmp;
}
function toMutableList_0(_this__u8e3s4) {
  return ArrayList_init_$Create$_1(_this__u8e3s4);
}
function first_0(_this__u8e3s4) {
  if (isInterface(_this__u8e3s4, KtList))
    return first(_this__u8e3s4);
  else {
    var iterator = _this__u8e3s4.g();
    if (!iterator.h())
      throw NoSuchElementException_init_$Create$_0('Collection is empty.');
    return iterator.i();
  }
}
function asSequence(_this__u8e3s4) {
  // Inline function 'kotlin.sequences.Sequence' call
  return new asSequence$$inlined$Sequence$1(_this__u8e3s4);
}
function asSequence$$inlined$Sequence$1($this_asSequence) {
  this.q_1 = $this_asSequence;
}
protoOf(asSequence$$inlined$Sequence$1).g = function () {
  return this.q_1.g();
};
function until(_this__u8e3s4, to) {
  if (to <= -2147483648)
    return Companion_getInstance_9().r_1;
  return numberRangeToNumber(_this__u8e3s4, to - 1 | 0);
}
function downTo(_this__u8e3s4, to) {
  return Companion_instance_10.s(_this__u8e3s4, to, -1);
}
function coerceAtMost(_this__u8e3s4, maximumValue) {
  return _this__u8e3s4 > maximumValue ? maximumValue : _this__u8e3s4;
}
function coerceAtLeast(_this__u8e3s4, minimumValue) {
  return _this__u8e3s4 < minimumValue ? minimumValue : _this__u8e3s4;
}
function coerceIn(_this__u8e3s4, minimumValue, maximumValue) {
  if (minimumValue > maximumValue)
    throw IllegalArgumentException_init_$Create$_0('Cannot coerce value to an empty range: maximum ' + maximumValue + ' is less than minimum ' + minimumValue + '.');
  if (_this__u8e3s4 < minimumValue)
    return minimumValue;
  if (_this__u8e3s4 > maximumValue)
    return maximumValue;
  return _this__u8e3s4;
}
function asIterable(_this__u8e3s4) {
  // Inline function 'kotlin.collections.Iterable' call
  return new asIterable$$inlined$Iterable$1(_this__u8e3s4);
}
function take_0(_this__u8e3s4, n) {
  // Inline function 'kotlin.require' call
  if (!(n >= 0)) {
    var message = 'Requested element count ' + n + ' is less than zero.';
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  var tmp;
  if (n === 0) {
    tmp = emptySequence();
  } else {
    if (isInterface(_this__u8e3s4, DropTakeSequence)) {
      tmp = _this__u8e3s4.t(n);
    } else {
      tmp = new TakeSequence(_this__u8e3s4, n);
    }
  }
  return tmp;
}
function map(_this__u8e3s4, transform) {
  return new TransformingSequence(_this__u8e3s4, transform);
}
function asIterable$$inlined$Iterable$1($this_asIterable) {
  this.u_1 = $this_asIterable;
}
protoOf(asIterable$$inlined$Iterable$1).g = function () {
  return this.u_1.g();
};
function last_0(_this__u8e3s4) {
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(_this__u8e3s4) === 0)
    throw NoSuchElementException_init_$Create$_0('Char sequence is empty.');
  return charSequenceGet(_this__u8e3s4, get_lastIndex_1(_this__u8e3s4));
}
function first_1(_this__u8e3s4) {
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(_this__u8e3s4) === 0)
    throw NoSuchElementException_init_$Create$_0('Char sequence is empty.');
  return charSequenceGet(_this__u8e3s4, 0);
}
function _Char___init__impl__6a9atx(value) {
  return value;
}
function _get_value__a43j40($this) {
  return $this;
}
function Char__compareTo_impl_ypi4mb($this, other) {
  return _get_value__a43j40($this) - _get_value__a43j40(other) | 0;
}
function Char__compareTo_impl_ypi4mb_0($this, other) {
  return Char__compareTo_impl_ypi4mb($this.v_1, other instanceof Char ? other.v_1 : THROW_CCE());
}
function Char__minus_impl_a2frrh($this, other) {
  return _get_value__a43j40($this) - _get_value__a43j40(other) | 0;
}
function Char__toInt_impl_vasixd($this) {
  return _get_value__a43j40($this);
}
function toString($this) {
  // Inline function 'kotlin.js.unsafeCast' call
  return String.fromCharCode(_get_value__a43j40($this));
}
function Char__equals_impl_x6719k($this, other) {
  if (!(other instanceof Char))
    return false;
  return _get_value__a43j40($this) === _get_value__a43j40(other.v_1);
}
function Char__hashCode_impl_otmys($this) {
  return _get_value__a43j40($this);
}
function Companion() {
  Companion_instance = this;
  this.w_1 = _Char___init__impl__6a9atx(0);
  this.x_1 = _Char___init__impl__6a9atx(65535);
  this.y_1 = _Char___init__impl__6a9atx(55296);
  this.z_1 = _Char___init__impl__6a9atx(56319);
  this.a1_1 = _Char___init__impl__6a9atx(56320);
  this.b1_1 = _Char___init__impl__6a9atx(57343);
  this.c1_1 = _Char___init__impl__6a9atx(55296);
  this.d1_1 = _Char___init__impl__6a9atx(57343);
  this.e1_1 = 2;
  this.f1_1 = 16;
}
var Companion_instance;
function Companion_getInstance() {
  if (Companion_instance == null)
    new Companion();
  return Companion_instance;
}
function Char(value) {
  Companion_getInstance();
  this.v_1 = value;
}
protoOf(Char).g1 = function (other) {
  return Char__compareTo_impl_ypi4mb(this.v_1, other);
};
protoOf(Char).d = function (other) {
  return Char__compareTo_impl_ypi4mb_0(this, other);
};
protoOf(Char).toString = function () {
  return toString(this.v_1);
};
protoOf(Char).equals = function (other) {
  return Char__equals_impl_x6719k(this.v_1, other);
};
protoOf(Char).hashCode = function () {
  return Char__hashCode_impl_otmys(this.v_1);
};
function Collection() {
}
function KtSet() {
}
function KtList() {
}
function Entry() {
}
function KtMap() {
}
function Companion_0() {
}
var Companion_instance_0;
function Companion_getInstance_0() {
  return Companion_instance_0;
}
function Enum(name, ordinal) {
  this.q1_1 = name;
  this.r1_1 = ordinal;
}
protoOf(Enum).s1 = function (other) {
  return compareTo(this.r1_1, other.r1_1);
};
protoOf(Enum).d = function (other) {
  return this.s1(other instanceof Enum ? other : THROW_CCE());
};
protoOf(Enum).equals = function (other) {
  return this === other;
};
protoOf(Enum).hashCode = function () {
  return identityHashCode(this);
};
protoOf(Enum).toString = function () {
  return this.q1_1;
};
function toString_0(_this__u8e3s4) {
  var tmp1_elvis_lhs = _this__u8e3s4 == null ? null : toString_1(_this__u8e3s4);
  return tmp1_elvis_lhs == null ? 'null' : tmp1_elvis_lhs;
}
function Companion_1() {
  Companion_instance_1 = this;
  this.t1_1 = new Long(0, -2147483648);
  this.u1_1 = new Long(-1, 2147483647);
  this.v1_1 = 8;
  this.w1_1 = 64;
}
var Companion_instance_1;
function Companion_getInstance_1() {
  if (Companion_instance_1 == null)
    new Companion_1();
  return Companion_instance_1;
}
function Long(low, high) {
  Companion_getInstance_1();
  Number_0.call(this);
  this.x1_1 = low;
  this.y1_1 = high;
}
protoOf(Long).z1 = function (other) {
  return compare(this, other);
};
protoOf(Long).d = function (other) {
  return this.z1(other instanceof Long ? other : THROW_CCE());
};
protoOf(Long).toString = function () {
  return toStringImpl(this, 10);
};
protoOf(Long).equals = function (other) {
  var tmp;
  if (other instanceof Long) {
    tmp = equalsLong(this, other);
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(Long).hashCode = function () {
  return hashCode(this);
};
protoOf(Long).valueOf = function () {
  return toNumber(this);
};
function abs(_this__u8e3s4) {
  var tmp;
  // Inline function 'kotlin.js.internal.isNegative' call
  if (_this__u8e3s4 < 0) {
    // Inline function 'kotlin.js.internal.unaryMinus' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    tmp = -_this__u8e3s4;
  } else {
    tmp = _this__u8e3s4;
  }
  return tmp;
}
function FunctionAdapter() {
}
function charArrayOf(arr) {
  var tmp0 = 'CharArray';
  // Inline function 'withType' call
  var array = new Uint16Array(arr);
  array.$type$ = tmp0;
  // Inline function 'kotlin.js.unsafeCast' call
  return array;
}
function get_buf() {
  _init_properties_bitUtils_kt__nfcg4k();
  return buf;
}
var buf;
function get_bufFloat64() {
  _init_properties_bitUtils_kt__nfcg4k();
  return bufFloat64;
}
var bufFloat64;
var bufFloat32;
function get_bufInt32() {
  _init_properties_bitUtils_kt__nfcg4k();
  return bufInt32;
}
var bufInt32;
function get_lowIndex() {
  _init_properties_bitUtils_kt__nfcg4k();
  return lowIndex;
}
var lowIndex;
function get_highIndex() {
  _init_properties_bitUtils_kt__nfcg4k();
  return highIndex;
}
var highIndex;
function getNumberHashCode(obj) {
  _init_properties_bitUtils_kt__nfcg4k();
  // Inline function 'kotlin.js.jsBitwiseOr' call
  // Inline function 'kotlin.js.unsafeCast' call
  // Inline function 'kotlin.js.asDynamic' call
  if ((obj | 0) === obj) {
    return numberToInt(obj);
  }
  get_bufFloat64()[0] = obj;
  return imul_0(get_bufInt32()[get_highIndex()], 31) + get_bufInt32()[get_lowIndex()] | 0;
}
var properties_initialized_bitUtils_kt_i2bo3e;
function _init_properties_bitUtils_kt__nfcg4k() {
  if (!properties_initialized_bitUtils_kt_i2bo3e) {
    properties_initialized_bitUtils_kt_i2bo3e = true;
    buf = new ArrayBuffer(8);
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    bufFloat64 = new Float64Array(get_buf());
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    bufFloat32 = new Float32Array(get_buf());
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    bufInt32 = new Int32Array(get_buf());
    // Inline function 'kotlin.run' call
    get_bufFloat64()[0] = -1.0;
    lowIndex = !(get_bufInt32()[0] === 0) ? 1 : 0;
    highIndex = 1 - get_lowIndex() | 0;
  }
}
function get_ZERO() {
  _init_properties_boxedLong_kt__v24qrw();
  return ZERO;
}
var ZERO;
function get_ONE() {
  _init_properties_boxedLong_kt__v24qrw();
  return ONE;
}
var ONE;
function get_NEG_ONE() {
  _init_properties_boxedLong_kt__v24qrw();
  return NEG_ONE;
}
var NEG_ONE;
function get_MAX_VALUE() {
  _init_properties_boxedLong_kt__v24qrw();
  return MAX_VALUE;
}
var MAX_VALUE;
function get_MIN_VALUE() {
  _init_properties_boxedLong_kt__v24qrw();
  return MIN_VALUE;
}
var MIN_VALUE;
function get_TWO_PWR_24_() {
  _init_properties_boxedLong_kt__v24qrw();
  return TWO_PWR_24_;
}
var TWO_PWR_24_;
var longArrayClass;
function compare(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  if (equalsLong(_this__u8e3s4, other)) {
    return 0;
  }
  var thisNeg = isNegative(_this__u8e3s4);
  var otherNeg = isNegative(other);
  return thisNeg && !otherNeg ? -1 : !thisNeg && otherNeg ? 1 : isNegative(subtract(_this__u8e3s4, other)) ? -1 : 1;
}
function convertToInt(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return _this__u8e3s4.x1_1;
}
function toNumber(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return _this__u8e3s4.y1_1 * 4.294967296E9 + getLowBitsUnsigned(_this__u8e3s4);
}
function toStringImpl(_this__u8e3s4, radix) {
  _init_properties_boxedLong_kt__v24qrw();
  if (isZero(_this__u8e3s4)) {
    return '0';
  }
  if (isNegative(_this__u8e3s4)) {
    if (equalsLong(_this__u8e3s4, get_MIN_VALUE())) {
      var radixLong = fromInt(radix);
      var div = divide(_this__u8e3s4, radixLong);
      var rem = convertToInt(subtract(multiply(div, radixLong), _this__u8e3s4));
      var tmp = toStringImpl(div, radix);
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      return tmp + rem.toString(radix);
    } else {
      return '-' + toStringImpl(negate(_this__u8e3s4), radix);
    }
  }
  var digitsPerTime = radix === 2 ? 31 : radix <= 10 ? 9 : radix <= 21 ? 7 : radix <= 35 ? 6 : 5;
  var radixToPower = fromNumber(Math.pow(radix, digitsPerTime));
  var rem_0 = _this__u8e3s4;
  var result = '';
  while (true) {
    var remDiv = divide(rem_0, radixToPower);
    var intval = convertToInt(subtract(rem_0, multiply(remDiv, radixToPower)));
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    var digits = intval.toString(radix);
    rem_0 = remDiv;
    if (isZero(rem_0)) {
      return digits + result;
    } else {
      while (digits.length < digitsPerTime) {
        digits = '0' + digits;
      }
      result = digits + result;
    }
  }
}
function equalsLong(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  return _this__u8e3s4.y1_1 === other.y1_1 && _this__u8e3s4.x1_1 === other.x1_1;
}
function hashCode(l) {
  _init_properties_boxedLong_kt__v24qrw();
  return l.x1_1 ^ l.y1_1;
}
function fromInt(value) {
  _init_properties_boxedLong_kt__v24qrw();
  return new Long(value, value < 0 ? -1 : 0);
}
function isNegative(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return _this__u8e3s4.y1_1 < 0;
}
function subtract(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  return add(_this__u8e3s4, negate(other));
}
function getLowBitsUnsigned(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return _this__u8e3s4.x1_1 >= 0 ? _this__u8e3s4.x1_1 : 4.294967296E9 + _this__u8e3s4.x1_1;
}
function isZero(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return _this__u8e3s4.y1_1 === 0 && _this__u8e3s4.x1_1 === 0;
}
function multiply(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  if (isZero(_this__u8e3s4)) {
    return get_ZERO();
  } else if (isZero(other)) {
    return get_ZERO();
  }
  if (equalsLong(_this__u8e3s4, get_MIN_VALUE())) {
    return isOdd(other) ? get_MIN_VALUE() : get_ZERO();
  } else if (equalsLong(other, get_MIN_VALUE())) {
    return isOdd(_this__u8e3s4) ? get_MIN_VALUE() : get_ZERO();
  }
  if (isNegative(_this__u8e3s4)) {
    var tmp;
    if (isNegative(other)) {
      tmp = multiply(negate(_this__u8e3s4), negate(other));
    } else {
      tmp = negate(multiply(negate(_this__u8e3s4), other));
    }
    return tmp;
  } else if (isNegative(other)) {
    return negate(multiply(_this__u8e3s4, negate(other)));
  }
  if (lessThan(_this__u8e3s4, get_TWO_PWR_24_()) && lessThan(other, get_TWO_PWR_24_())) {
    return fromNumber(toNumber(_this__u8e3s4) * toNumber(other));
  }
  var a48 = _this__u8e3s4.y1_1 >>> 16 | 0;
  var a32 = _this__u8e3s4.y1_1 & 65535;
  var a16 = _this__u8e3s4.x1_1 >>> 16 | 0;
  var a00 = _this__u8e3s4.x1_1 & 65535;
  var b48 = other.y1_1 >>> 16 | 0;
  var b32 = other.y1_1 & 65535;
  var b16 = other.x1_1 >>> 16 | 0;
  var b00 = other.x1_1 & 65535;
  var c48 = 0;
  var c32 = 0;
  var c16 = 0;
  var c00 = 0;
  c00 = c00 + imul_0(a00, b00) | 0;
  c16 = c16 + (c00 >>> 16 | 0) | 0;
  c00 = c00 & 65535;
  c16 = c16 + imul_0(a16, b00) | 0;
  c32 = c32 + (c16 >>> 16 | 0) | 0;
  c16 = c16 & 65535;
  c16 = c16 + imul_0(a00, b16) | 0;
  c32 = c32 + (c16 >>> 16 | 0) | 0;
  c16 = c16 & 65535;
  c32 = c32 + imul_0(a32, b00) | 0;
  c48 = c48 + (c32 >>> 16 | 0) | 0;
  c32 = c32 & 65535;
  c32 = c32 + imul_0(a16, b16) | 0;
  c48 = c48 + (c32 >>> 16 | 0) | 0;
  c32 = c32 & 65535;
  c32 = c32 + imul_0(a00, b32) | 0;
  c48 = c48 + (c32 >>> 16 | 0) | 0;
  c32 = c32 & 65535;
  c48 = c48 + (((imul_0(a48, b00) + imul_0(a32, b16) | 0) + imul_0(a16, b32) | 0) + imul_0(a00, b48) | 0) | 0;
  c48 = c48 & 65535;
  return new Long(c16 << 16 | c00, c48 << 16 | c32);
}
function negate(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return add(invert(_this__u8e3s4), new Long(1, 0));
}
function fromNumber(value) {
  _init_properties_boxedLong_kt__v24qrw();
  if (isNaN_0(value)) {
    return get_ZERO();
  } else if (value <= -9.223372036854776E18) {
    return get_MIN_VALUE();
  } else if (value + 1 >= 9.223372036854776E18) {
    return get_MAX_VALUE();
  } else if (value < 0) {
    return negate(fromNumber(-value));
  } else {
    var twoPwr32 = 4.294967296E9;
    // Inline function 'kotlin.js.jsBitwiseOr' call
    var tmp = value % twoPwr32 | 0;
    // Inline function 'kotlin.js.jsBitwiseOr' call
    var tmp$ret$1 = value / twoPwr32 | 0;
    return new Long(tmp, tmp$ret$1);
  }
}
function add(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  var a48 = _this__u8e3s4.y1_1 >>> 16 | 0;
  var a32 = _this__u8e3s4.y1_1 & 65535;
  var a16 = _this__u8e3s4.x1_1 >>> 16 | 0;
  var a00 = _this__u8e3s4.x1_1 & 65535;
  var b48 = other.y1_1 >>> 16 | 0;
  var b32 = other.y1_1 & 65535;
  var b16 = other.x1_1 >>> 16 | 0;
  var b00 = other.x1_1 & 65535;
  var c48 = 0;
  var c32 = 0;
  var c16 = 0;
  var c00 = 0;
  c00 = c00 + (a00 + b00 | 0) | 0;
  c16 = c16 + (c00 >>> 16 | 0) | 0;
  c00 = c00 & 65535;
  c16 = c16 + (a16 + b16 | 0) | 0;
  c32 = c32 + (c16 >>> 16 | 0) | 0;
  c16 = c16 & 65535;
  c32 = c32 + (a32 + b32 | 0) | 0;
  c48 = c48 + (c32 >>> 16 | 0) | 0;
  c32 = c32 & 65535;
  c48 = c48 + (a48 + b48 | 0) | 0;
  c48 = c48 & 65535;
  return new Long(c16 << 16 | c00, c48 << 16 | c32);
}
function isOdd(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return (_this__u8e3s4.x1_1 & 1) === 1;
}
function lessThan(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  return compare(_this__u8e3s4, other) < 0;
}
function invert(_this__u8e3s4) {
  _init_properties_boxedLong_kt__v24qrw();
  return new Long(~_this__u8e3s4.x1_1, ~_this__u8e3s4.y1_1);
}
function divide(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  if (isZero(other)) {
    throw Exception_init_$Create$_0('division by zero');
  } else if (isZero(_this__u8e3s4)) {
    return get_ZERO();
  }
  if (equalsLong(_this__u8e3s4, get_MIN_VALUE())) {
    if (equalsLong(other, get_ONE()) || equalsLong(other, get_NEG_ONE())) {
      return get_MIN_VALUE();
    } else if (equalsLong(other, get_MIN_VALUE())) {
      return get_ONE();
    } else {
      var halfThis = shiftRight(_this__u8e3s4, 1);
      var approx = shiftLeft(divide(halfThis, other), 1);
      if (equalsLong(approx, get_ZERO())) {
        return isNegative(other) ? get_ONE() : get_NEG_ONE();
      } else {
        var rem = subtract(_this__u8e3s4, multiply(other, approx));
        return add(approx, divide(rem, other));
      }
    }
  } else if (equalsLong(other, get_MIN_VALUE())) {
    return get_ZERO();
  }
  if (isNegative(_this__u8e3s4)) {
    var tmp;
    if (isNegative(other)) {
      tmp = divide(negate(_this__u8e3s4), negate(other));
    } else {
      tmp = negate(divide(negate(_this__u8e3s4), other));
    }
    return tmp;
  } else if (isNegative(other)) {
    return negate(divide(_this__u8e3s4, negate(other)));
  }
  var res = get_ZERO();
  var rem_0 = _this__u8e3s4;
  while (greaterThanOrEqual(rem_0, other)) {
    var approxDouble = toNumber(rem_0) / toNumber(other);
    var approx2 = Math.max(1.0, Math.floor(approxDouble));
    var log2 = Math.ceil(Math.log(approx2) / Math.LN2);
    var delta = log2 <= 48 ? 1.0 : Math.pow(2.0, log2 - 48);
    var approxRes = fromNumber(approx2);
    var approxRem = multiply(approxRes, other);
    while (isNegative(approxRem) || greaterThan(approxRem, rem_0)) {
      approx2 = approx2 - delta;
      approxRes = fromNumber(approx2);
      approxRem = multiply(approxRes, other);
    }
    if (isZero(approxRes)) {
      approxRes = get_ONE();
    }
    res = add(res, approxRes);
    rem_0 = subtract(rem_0, approxRem);
  }
  return res;
}
function shiftRight(_this__u8e3s4, numBits) {
  _init_properties_boxedLong_kt__v24qrw();
  var numBits_0 = numBits & 63;
  if (numBits_0 === 0) {
    return _this__u8e3s4;
  } else {
    if (numBits_0 < 32) {
      return new Long(_this__u8e3s4.x1_1 >>> numBits_0 | 0 | _this__u8e3s4.y1_1 << (32 - numBits_0 | 0), _this__u8e3s4.y1_1 >> numBits_0);
    } else {
      return new Long(_this__u8e3s4.y1_1 >> (numBits_0 - 32 | 0), _this__u8e3s4.y1_1 >= 0 ? 0 : -1);
    }
  }
}
function shiftLeft(_this__u8e3s4, numBits) {
  _init_properties_boxedLong_kt__v24qrw();
  var numBits_0 = numBits & 63;
  if (numBits_0 === 0) {
    return _this__u8e3s4;
  } else {
    if (numBits_0 < 32) {
      return new Long(_this__u8e3s4.x1_1 << numBits_0, _this__u8e3s4.y1_1 << numBits_0 | (_this__u8e3s4.x1_1 >>> (32 - numBits_0 | 0) | 0));
    } else {
      return new Long(0, _this__u8e3s4.x1_1 << (numBits_0 - 32 | 0));
    }
  }
}
function greaterThan(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  return compare(_this__u8e3s4, other) > 0;
}
function greaterThanOrEqual(_this__u8e3s4, other) {
  _init_properties_boxedLong_kt__v24qrw();
  return compare(_this__u8e3s4, other) >= 0;
}
function isLongArray(a) {
  _init_properties_boxedLong_kt__v24qrw();
  return isJsArray(a) && a.$type$ === 'LongArray';
}
function longArrayClass$lambda(it) {
  _init_properties_boxedLong_kt__v24qrw();
  return !(it == null) ? isLongArray(it) : false;
}
var properties_initialized_boxedLong_kt_lfwt2;
function _init_properties_boxedLong_kt__v24qrw() {
  if (!properties_initialized_boxedLong_kt_lfwt2) {
    properties_initialized_boxedLong_kt_lfwt2 = true;
    ZERO = fromInt(0);
    ONE = fromInt(1);
    NEG_ONE = fromInt(-1);
    MAX_VALUE = new Long(-1, 2147483647);
    MIN_VALUE = new Long(0, -2147483648);
    TWO_PWR_24_ = fromInt(16777216);
    // Inline function 'kotlin.js.unsafeCast' call
    var tmp = Array;
    longArrayClass = new PrimitiveKClassImpl(tmp, 'LongArray', longArrayClass$lambda);
  }
}
function charSequenceGet(a, index) {
  var tmp;
  if (isString(a)) {
    tmp = charCodeAt(a, index);
  } else {
    tmp = a.b(index);
  }
  return tmp;
}
function isString(a) {
  return typeof a === 'string';
}
function charCodeAt(_this__u8e3s4, index) {
  // Inline function 'kotlin.js.asDynamic' call
  return _this__u8e3s4.charCodeAt(index);
}
function charSequenceLength(a) {
  var tmp;
  if (isString(a)) {
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    tmp = a.length;
  } else {
    tmp = a.a();
  }
  return tmp;
}
function charSequenceSubSequence(a, startIndex, endIndex) {
  var tmp;
  if (isString(a)) {
    tmp = substring(a, startIndex, endIndex);
  } else {
    tmp = a.c(startIndex, endIndex);
  }
  return tmp;
}
function arrayToString(array) {
  return joinToString(array, ', ', '[', ']', VOID, VOID, arrayToString$lambda);
}
function contentEqualsInternal(_this__u8e3s4, other) {
  // Inline function 'kotlin.js.asDynamic' call
  var a = _this__u8e3s4;
  // Inline function 'kotlin.js.asDynamic' call
  var b = other;
  if (a === b)
    return true;
  if (a == null || b == null || !isArrayish(b) || a.length != b.length)
    return false;
  var inductionVariable = 0;
  var last = a.length;
  if (inductionVariable < last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      if (!equals(a[i], b[i])) {
        return false;
      }
    }
     while (inductionVariable < last);
  return true;
}
function contentHashCodeInternal(_this__u8e3s4) {
  // Inline function 'kotlin.js.asDynamic' call
  var a = _this__u8e3s4;
  if (a == null)
    return 0;
  var result = 1;
  var inductionVariable = 0;
  var last = a.length;
  if (inductionVariable < last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      result = imul_0(result, 31) + hashCode_0(a[i]) | 0;
    }
     while (inductionVariable < last);
  return result;
}
function arrayToString$lambda(it) {
  return toString_1(it);
}
function compareTo(a, b) {
  var tmp;
  switch (typeof a) {
    case 'number':
      var tmp_0;
      if (typeof b === 'number') {
        tmp_0 = doubleCompareTo(a, b);
      } else {
        if (b instanceof Long) {
          tmp_0 = doubleCompareTo(a, toNumber(b));
        } else {
          tmp_0 = primitiveCompareTo(a, b);
        }
      }

      tmp = tmp_0;
      break;
    case 'string':
    case 'boolean':
    case 'bigint':
      tmp = primitiveCompareTo(a, b);
      break;
    default:
      tmp = compareToDoNotIntrinsicify(a, b);
      break;
  }
  return tmp;
}
function doubleCompareTo(a, b) {
  var tmp;
  if (a < b) {
    tmp = -1;
  } else if (a > b) {
    tmp = 1;
  } else if (a === b) {
    var tmp_0;
    if (a !== 0) {
      tmp_0 = 0;
    } else {
      // Inline function 'kotlin.js.asDynamic' call
      var ia = 1 / a;
      var tmp_1;
      // Inline function 'kotlin.js.asDynamic' call
      if (ia === 1 / b) {
        tmp_1 = 0;
      } else {
        if (ia < 0) {
          tmp_1 = -1;
        } else {
          tmp_1 = 1;
        }
      }
      tmp_0 = tmp_1;
    }
    tmp = tmp_0;
  } else if (a !== a) {
    tmp = b !== b ? 0 : 1;
  } else {
    tmp = -1;
  }
  return tmp;
}
function primitiveCompareTo(a, b) {
  return a < b ? -1 : a > b ? 1 : 0;
}
function compareToDoNotIntrinsicify(a, b) {
  return a.d(b);
}
function identityHashCode(obj) {
  return getObjectHashCode(obj);
}
function objectCreate(proto) {
  proto = proto === VOID ? null : proto;
  return Object.create(proto);
}
function defineProp(obj, name, getter, setter, enumerable) {
  return Object.defineProperty(obj, name, {configurable: true, get: getter, set: setter, enumerable: enumerable});
}
function getObjectHashCode(obj) {
  // Inline function 'kotlin.js.jsIn' call
  if (!('kotlinHashCodeValue$' in obj)) {
    var hash = calculateRandomHash();
    var descriptor = new Object();
    descriptor.value = hash;
    descriptor.enumerable = false;
    Object.defineProperty(obj, 'kotlinHashCodeValue$', descriptor);
  }
  // Inline function 'kotlin.js.unsafeCast' call
  return obj['kotlinHashCodeValue$'];
}
function calculateRandomHash() {
  // Inline function 'kotlin.js.jsBitwiseOr' call
  return Math.random() * 4.294967296E9 | 0;
}
function toString_1(o) {
  var tmp;
  if (o == null) {
    tmp = 'null';
  } else if (isArrayish(o)) {
    tmp = '[...]';
  } else if (!(typeof o.toString === 'function')) {
    tmp = anyToString(o);
  } else {
    // Inline function 'kotlin.js.unsafeCast' call
    tmp = o.toString();
  }
  return tmp;
}
function anyToString(o) {
  return Object.prototype.toString.call(o);
}
function equals(obj1, obj2) {
  if (obj1 == null) {
    return obj2 == null;
  }
  if (obj2 == null) {
    return false;
  }
  if (typeof obj1 === 'object' && typeof obj1.equals === 'function') {
    return obj1.equals(obj2);
  }
  if (obj1 !== obj1) {
    return obj2 !== obj2;
  }
  if (typeof obj1 === 'number' && typeof obj2 === 'number') {
    var tmp;
    if (obj1 === obj2) {
      var tmp_0;
      if (obj1 !== 0) {
        tmp_0 = true;
      } else {
        // Inline function 'kotlin.js.asDynamic' call
        var tmp_1 = 1 / obj1;
        // Inline function 'kotlin.js.asDynamic' call
        tmp_0 = tmp_1 === 1 / obj2;
      }
      tmp = tmp_0;
    } else {
      tmp = false;
    }
    return tmp;
  }
  if (isCallableReference(obj1) && isCallableReference(obj2)) {
    if (obj1 === obj2)
      return true;
    if (obj1.$id != obj2.$id)
      return false;
    if (obj1.$flags != obj2.$flags)
      return false;
    if (obj1.$arity != obj2.$arity)
      return false;
    if (obj1.$bound == null && obj2.$bound == null)
      return true;
    if (obj1.$bound === obj2.$bound)
      return true;
    if (!isJsArray(obj1.$bound) || !isJsArray(obj2.$bound))
      return false;
    // Inline function 'kotlin.js.unsafeCast' call
    var bound1 = obj1.$bound;
    // Inline function 'kotlin.js.unsafeCast' call
    var bound2 = obj2.$bound;
    return contentEqualsInternal(bound1, bound2);
  }
  return obj1 === obj2;
}
function hashCode_0(obj) {
  if (obj == null)
    return 0;
  var typeOf = typeof obj;
  var tmp;
  switch (typeOf) {
    case 'object':
      tmp = 'function' === typeof obj.hashCode ? obj.hashCode() : getObjectHashCode(obj);
      break;
    case 'function':
      tmp = isCallableReference(obj) ? getCallableReferenceHashCode(obj) : getObjectHashCode(obj);
      break;
    case 'number':
      tmp = getNumberHashCode(obj);
      break;
    case 'boolean':
      // Inline function 'kotlin.js.unsafeCast' call

      tmp = getBooleanHashCode(obj);
      break;
    case 'string':
      tmp = getStringHashCode(String(obj));
      break;
    case 'bigint':
      // Inline function 'kotlin.js.unsafeCast' call

      tmp = getBigIntHashCode(obj);
      break;
    case 'symbol':
      tmp = getSymbolHashCode(obj);
      break;
    default:
      tmp = function () {
        throw new Error('Unexpected typeof `' + typeOf + '`');
      }();
      break;
  }
  return tmp;
}
function getCallableReferenceHashCode(obj) {
  // Inline function 'kotlin.js.unsafeCast' call
  var hash = obj.$flags;
  hash = imul_0(31, hash) + hashCode_0(obj.$id) | 0;
  var tmp = imul_0(31, hash);
  var tmp0_elvis_lhs = obj.$arity;
  // Inline function 'kotlin.js.unsafeCast' call
  hash = tmp + (tmp0_elvis_lhs == null ? -1 : tmp0_elvis_lhs) | 0;
  var bound = obj.$bound;
  if (bound != null && isJsArray(bound)) {
    // Inline function 'kotlin.js.unsafeCast' call
    var boundArray = bound;
    hash = imul_0(31, hash) + contentHashCodeInternal(boundArray) | 0;
  }
  return hash;
}
function getBooleanHashCode(value) {
  return value ? 1231 : 1237;
}
function getStringHashCode(str) {
  var hash = 0;
  var length = str.length;
  var inductionVariable = 0;
  var last = length - 1 | 0;
  if (inductionVariable <= last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      // Inline function 'kotlin.js.asDynamic' call
      var code = str.charCodeAt(i);
      hash = imul_0(hash, 31) + code | 0;
    }
     while (!(i === last));
  return hash;
}
function getBigIntHashCode(value) {
  var shiftNumber = BigInt(32);
  var mask = BigInt(4.294967295E9);
  var bigNumber = abs(value);
  var hashCode = 0;
  var tmp;
  // Inline function 'kotlin.js.internal.isNegative' call
  if (value < 0) {
    tmp = -1;
  } else {
    tmp = 1;
  }
  var signum = tmp;
  $l$loop: while (true) {
    // Inline function 'kotlin.js.internal.isZero' call
    if (!!(bigNumber == 0)) {
      break $l$loop;
    }
    // Inline function 'kotlin.js.internal.and' call
    // Inline function 'kotlin.js.jsBitwiseAnd' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.internal.toNumber' call
    var self_0 = bigNumber & mask;
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    var chunk = Number(self_0);
    hashCode = imul_0(31, hashCode) + chunk | 0;
    // Inline function 'kotlin.js.internal.shr' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    bigNumber = bigNumber >> shiftNumber;
  }
  return imul_0(hashCode, signum);
}
function getSymbolHashCode(value) {
  var hashCodeMap = symbolIsSharable(value) ? getSymbolMap() : getSymbolWeakMap();
  var cachedHashCode = hashCodeMap.get(value);
  if (cachedHashCode !== VOID)
    return cachedHashCode;
  var hash = calculateRandomHash();
  hashCodeMap.set(value, hash);
  return hash;
}
function symbolIsSharable(symbol) {
  return Symbol.keyFor(symbol) != VOID;
}
function getSymbolMap() {
  if (symbolMap === VOID) {
    symbolMap = new Map();
  }
  return symbolMap;
}
function getSymbolWeakMap() {
  if (symbolWeakMap === VOID) {
    symbolWeakMap = new WeakMap();
  }
  return symbolWeakMap;
}
var symbolMap;
var symbolWeakMap;
function boxIntrinsic(x) {
  // Inline function 'kotlin.error' call
  var message = 'Should be lowered';
  throw IllegalStateException_init_$Create$_0(toString_1(message));
}
function unboxIntrinsic(x) {
  // Inline function 'kotlin.error' call
  var message = 'Should be lowered';
  throw IllegalStateException_init_$Create$_0(toString_1(message));
}
function captureStack(instance, constructorFunction) {
  if (Error.captureStackTrace != null) {
    Error.captureStackTrace(instance, constructorFunction);
  } else {
    // Inline function 'kotlin.js.asDynamic' call
    instance.stack = (new Error()).stack;
  }
}
function protoOf(constructor) {
  return constructor.prototype;
}
function defineMessage(message, cause) {
  var tmp;
  if (isUndefined(message)) {
    var tmp_0;
    if (isUndefined(cause)) {
      tmp_0 = message;
    } else {
      var tmp1_elvis_lhs = cause == null ? null : cause.toString();
      tmp_0 = tmp1_elvis_lhs == null ? VOID : tmp1_elvis_lhs;
    }
    tmp = tmp_0;
  } else {
    tmp = message == null ? VOID : message;
  }
  return tmp;
}
function isUndefined(value) {
  return value === VOID;
}
function extendThrowable(this_, message, cause) {
  defineFieldOnInstance(this_, 'message', defineMessage(message, cause));
  defineFieldOnInstance(this_, 'cause', cause);
  defineFieldOnInstance(this_, 'name', Object.getPrototypeOf(this_).constructor.name);
}
function defineFieldOnInstance(this_, name, value) {
  Object.defineProperty(this_, name, {configurable: true, writable: true, value: value});
}
function noWhenBranchMatchedException() {
  throw NoWhenBranchMatchedException_init_$Create$();
}
function THROW_NPE() {
  throw NullPointerException_init_$Create$();
}
function THROW_CCE() {
  throw ClassCastException_init_$Create$();
}
function ensureNotNull(v) {
  var tmp;
  if (v == null) {
    THROW_NPE();
  } else {
    tmp = v;
  }
  return tmp;
}
function jsGenerateInterfaceSymbol() {
  return generateInterfaceSymbolById();
}
function createMetadata(kind, name, defaultConstructor, associatedObjectKey, associatedObjects, suspendArity) {
  var undef = VOID;
  return {kind: kind, simpleName: name, associatedObjectKey: associatedObjectKey, associatedObjects: associatedObjects, suspendArity: suspendArity, $kClass$: undef, defaultConstructor: defaultConstructor};
}
function initMetadataForClass(ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
  var kind = 'class';
  initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects);
}
function initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
  if (!(parent == null)) {
    ctor.prototype = Object.create(parent.prototype);
    ctor.prototype.constructor = ctor;
  }
  var metadata = createMetadata(kind, name, defaultConstructor, associatedObjectKey, associatedObjects, suspendArity);
  ctor.$metadata$ = metadata;
  var prototype = ctor.prototype;
  if (!(interfaces == null)) {
    var inductionVariable = 0;
    var last = interfaces.length;
    while (inductionVariable < last) {
      var i = interfaces[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      Object.assign(prototype, i.prototype);
      prototype[i.Symbol] = true;
    }
  }
  if (kind === 'interface') {
    ctor.Symbol = generateInterfaceSymbolById();
  }
}
function generateInterfaceSymbolById() {
  return '#__interface_' + generateInterfaceId();
}
function generateInterfaceId() {
  if (globalInterfaceId === VOID) {
    globalInterfaceId = 0;
  }
  // Inline function 'kotlin.js.unsafeCast' call
  globalInterfaceId = globalInterfaceId + 1 | 0;
  return globalInterfaceId;
}
var globalInterfaceId;
function initMetadataForObject(ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
  var kind = 'object';
  initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects);
}
function initMetadataForInterface(ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects) {
  var kind = 'interface';
  initMetadataFor(kind, ctor, name, defaultConstructor, parent, interfaces, suspendArity, associatedObjectKey, associatedObjects);
}
function initMetadataForLambda(ctor, parent, interfaces, suspendArity) {
  initMetadataForClass(ctor, 'Lambda', VOID, parent, interfaces, suspendArity, VOID, VOID);
}
function initMetadataForCoroutine(ctor, parent, interfaces, suspendArity) {
  initMetadataForClass(ctor, 'Coroutine', VOID, parent, interfaces, suspendArity, VOID, VOID);
}
function initMetadataForFunctionReference(ctor, parent, interfaces, suspendArity) {
  initMetadataForClass(ctor, 'FunctionReference', VOID, parent, interfaces, suspendArity, VOID, VOID);
}
function initMetadataForCompanion(ctor, parent, interfaces, suspendArity) {
  initMetadataForObject(ctor, 'Companion', VOID, parent, interfaces, suspendArity, VOID, VOID);
}
function numberToInt(a) {
  var tmp;
  if (a instanceof Long) {
    tmp = convertToInt(a);
  } else {
    tmp = doubleToInt(a);
  }
  return tmp;
}
function doubleToInt(a) {
  var tmp;
  if (a > 2147483647) {
    tmp = 2147483647;
  } else if (a < -2147483648) {
    tmp = -2147483648;
  } else {
    // Inline function 'kotlin.js.jsBitwiseOr' call
    tmp = a | 0;
  }
  return tmp;
}
function numberRangeToNumber(start, endInclusive) {
  return new IntRange(start, endInclusive);
}
function get_propertyRefClassMetadataCache() {
  _init_properties_reflectRuntime_kt__5r4uu3();
  return propertyRefClassMetadataCache;
}
var propertyRefClassMetadataCache;
function metadataObject() {
  _init_properties_reflectRuntime_kt__5r4uu3();
  return createMetadata('class', VOID, VOID, VOID, VOID, VOID);
}
function getPropertyCallableRef(name, paramCount, superType, getter, setter, linkageError) {
  _init_properties_reflectRuntime_kt__5r4uu3();
  getter.get = getter;
  getter.set = setter;
  getter.callableName = name;
  // Inline function 'kotlin.js.unsafeCast' call
  return getPropertyRefClass(getter, getKPropMetadata(paramCount, setter), superType);
}
function getPropertyRefClass(obj, metadata, superType) {
  _init_properties_reflectRuntime_kt__5r4uu3();
  obj.$metadata$ = metadata;
  obj.constructor = obj;
  var symbol = superType.Symbol;
  if (symbol != null) {
    // Inline function 'kotlin.js.asDynamic' call
    obj[symbol] = true;
  }
  Object.assign(obj, superType.prototype);
  return obj;
}
function getKPropMetadata(paramCount, setter) {
  _init_properties_reflectRuntime_kt__5r4uu3();
  return get_propertyRefClassMetadataCache()[paramCount][setter == null ? 0 : 1];
}
function constructCallableReference(callable, arity, flags, signatureId, name, bounds) {
  _init_properties_reflectRuntime_kt__5r4uu3();
  callable.callableName = name;
  callable.$flags = flags;
  callable.$arity = arity;
  callable.$id = signatureId;
  callable.$bound = bounds;
  return callable;
}
var properties_initialized_reflectRuntime_kt_inkhwd;
function _init_properties_reflectRuntime_kt__5r4uu3() {
  if (!properties_initialized_reflectRuntime_kt_inkhwd) {
    properties_initialized_reflectRuntime_kt_inkhwd = true;
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp = [metadataObject(), metadataObject()];
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp_0 = [metadataObject(), metadataObject()];
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.arrayOf' call
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    propertyRefClassMetadataCache = [tmp, tmp_0, [metadataObject(), metadataObject()]];
  }
}
function isArrayish(o) {
  return isJsArray(o) || isView(o);
}
function isJsArray(obj) {
  // Inline function 'kotlin.js.unsafeCast' call
  return Array.isArray(obj);
}
function isCallableReference(value) {
  return typeof value === 'function' && value.$flags != null && value.$arity != null;
}
function isInterface(obj, iface) {
  return obj[iface.Symbol] === true;
}
function isArray(obj) {
  var tmp;
  if (isJsArray(obj)) {
    // Inline function 'kotlin.js.asDynamic' call
    tmp = !obj.$type$;
  } else {
    tmp = false;
  }
  return tmp;
}
function isNumber(a) {
  var tmp;
  if (typeof a === 'number') {
    tmp = true;
  } else {
    tmp = a instanceof Long;
  }
  return tmp;
}
function isComparable(value) {
  var type = typeof value;
  return type === 'string' || type === 'boolean' || isNumber(value) || isInterface(value, Comparable);
}
function isCharSequence(value) {
  return typeof value === 'string' || isInterface(value, CharSequence);
}
function get_VOID() {
  _init_properties_void_kt__3zg9as();
  return VOID;
}
var VOID;
var properties_initialized_void_kt_e4ret2;
function _init_properties_void_kt__3zg9as() {
  if (!properties_initialized_void_kt_e4ret2) {
    properties_initialized_void_kt_e4ret2 = true;
    VOID = void 0;
  }
}
function copyOf(_this__u8e3s4, newSize) {
  // Inline function 'kotlin.require' call
  if (!(newSize >= 0)) {
    var message = 'Invalid new array size: ' + newSize + '.';
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  var size = _this__u8e3s4.length;
  var tmp;
  if (newSize < 16 || size < 16) {
    tmp = fillFrom(_this__u8e3s4, new Int32Array(newSize));
  } else if (newSize > size) {
    // Inline function 'kotlin.also' call
    var this_0 = new Int32Array(newSize);
    // Inline function 'kotlin.js.asDynamic' call
    this_0.set(_this__u8e3s4);
    tmp = this_0;
  } else {
    // Inline function 'kotlin.js.asDynamic' call
    tmp = _this__u8e3s4.slice(0, newSize);
  }
  return tmp;
}
function copyOf_0(_this__u8e3s4, newSize) {
  // Inline function 'kotlin.require' call
  if (!(newSize >= 0)) {
    var message = 'Invalid new array size: ' + newSize + '.';
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  return arrayCopyResize(_this__u8e3s4, newSize, null);
}
function contentEquals(_this__u8e3s4, other) {
  return contentEqualsInternal(_this__u8e3s4, other);
}
function asList(_this__u8e3s4) {
  // Inline function 'kotlin.js.unsafeCast' call
  // Inline function 'kotlin.js.asDynamic' call
  return new ArrayList(_this__u8e3s4);
}
function sort(_this__u8e3s4) {
  if (_this__u8e3s4.length > 1) {
    sortArray(_this__u8e3s4);
  }
}
function decodeVarLenBase64(base64, fromBase64, resultLength) {
  var result = new Int32Array(resultLength);
  var index = 0;
  var int = 0;
  var shift = 0;
  var inductionVariable = 0;
  var last = base64.length;
  while (inductionVariable < last) {
    var char = charCodeAt(base64, inductionVariable);
    inductionVariable = inductionVariable + 1 | 0;
    // Inline function 'kotlin.code' call
    var sixBit = fromBase64[Char__toInt_impl_vasixd(char)];
    int = int | (sixBit & 31) << shift;
    if (sixBit < 32) {
      var _unary__edvuaz = index;
      index = _unary__edvuaz + 1 | 0;
      result[_unary__edvuaz] = int;
      int = 0;
      shift = 0;
    } else {
      shift = shift + 5 | 0;
    }
  }
  return result;
}
function isDigitImpl(_this__u8e3s4) {
  return digitToIntImpl(_this__u8e3s4) >= 0;
}
function digitToIntImpl(_this__u8e3s4) {
  // Inline function 'kotlin.code' call
  var ch = Char__toInt_impl_vasixd(_this__u8e3s4);
  var index = binarySearchRange(Digit_getInstance().b2_1, ch);
  var diff = ch - Digit_getInstance().b2_1[index] | 0;
  return diff < 10 ? diff : -1;
}
function binarySearchRange(array, needle) {
  var bottom = 0;
  var top = array.length - 1 | 0;
  var middle = -1;
  var value = 0;
  while (bottom <= top) {
    middle = (bottom + top | 0) / 2 | 0;
    value = array[middle];
    if (needle > value)
      bottom = middle + 1 | 0;
    else if (needle === value)
      return middle;
    else
      top = middle - 1 | 0;
  }
  return middle - (needle < value ? 1 : 0) | 0;
}
function Digit() {
  Digit_instance = this;
  var tmp = this;
  // Inline function 'kotlin.intArrayOf' call
  tmp.b2_1 = new Int32Array([48, 1632, 1776, 1984, 2406, 2534, 2662, 2790, 2918, 3046, 3174, 3302, 3430, 3558, 3664, 3792, 3872, 4160, 4240, 6112, 6160, 6470, 6608, 6784, 6800, 6992, 7088, 7232, 7248, 42528, 43216, 43264, 43472, 43504, 43600, 44016, 65296]);
}
var Digit_instance;
function Digit_getInstance() {
  if (Digit_instance == null)
    new Digit();
  return Digit_instance;
}
function isLetterImpl(_this__u8e3s4) {
  return !(getLetterType(_this__u8e3s4) === 0);
}
function getLetterType(_this__u8e3s4) {
  // Inline function 'kotlin.code' call
  var ch = Char__toInt_impl_vasixd(_this__u8e3s4);
  var index = binarySearchRange(Letter_getInstance().c2_1, ch);
  var rangeStart = Letter_getInstance().c2_1[index];
  var rangeEnd = (rangeStart + Letter_getInstance().d2_1[index] | 0) - 1 | 0;
  var code = Letter_getInstance().e2_1[index];
  if (ch > rangeEnd) {
    return 0;
  }
  var lastTwoBits = code & 3;
  if (lastTwoBits === 0) {
    var shift = 2;
    var threshold = rangeStart;
    var inductionVariable = 0;
    if (inductionVariable <= 1)
      do {
        var i = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        threshold = threshold + (code >> shift & 127) | 0;
        if (threshold > ch) {
          return 3;
        }
        shift = shift + 7 | 0;
        threshold = threshold + (code >> shift & 127) | 0;
        if (threshold > ch) {
          return 0;
        }
        shift = shift + 7 | 0;
      }
       while (inductionVariable <= 1);
    return 3;
  }
  if (code <= 7) {
    return lastTwoBits;
  }
  var distance = ch - rangeStart | 0;
  var shift_0 = code <= 31 ? distance % 2 | 0 : distance;
  return code >> imul_0(2, shift_0) & 3;
}
function Letter() {
  Letter_instance = this;
  var toBase64 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
  var fromBase64 = new Int32Array(128);
  var inductionVariable = 0;
  var last = charSequenceLength(toBase64) - 1 | 0;
  if (inductionVariable <= last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      // Inline function 'kotlin.code' call
      var this_0 = charCodeAt(toBase64, i);
      fromBase64[Char__toInt_impl_vasixd(this_0)] = i;
    }
     while (inductionVariable <= last);
  var rangeStartDiff = 'hCgBpCQGYHZH5BRpBPPPPPPRMP5BPPlCPP6BkEPPPPcPXPzBvBrB3BOiDoBHwD+E3DauCnFmBmB2D6E1BlBTiBmBlBP5BhBiBrBvBjBqBnBPRtBiCmCtBlB0BmB5BiB7BmBgEmChBZgCoEoGVpBSfRhBPqKQ2BwBYoFgB4CJuTiEvBuCuDrF5DgEgFlJ1DgFmBQtBsBRGsB+BPiBlD1EIjDPRPPPQPPPPPGQSQS/DxENVNU+B9zCwBwBPPCkDPNnBPqDYY1R8B7FkFgTgwGgwUwmBgKwBuBScmEP/BPPPPPPrBP8B7F1B/ErBqC6B7BiBmBfQsBUwCw/KwqIwLwETPcPjQgJxFgBlBsD';
  var diff = decodeVarLenBase64(rangeStartDiff, fromBase64, 222);
  var start = new Int32Array(diff.length);
  var inductionVariable_0 = 0;
  var last_0 = diff.length - 1 | 0;
  if (inductionVariable_0 <= last_0)
    do {
      var i_0 = inductionVariable_0;
      inductionVariable_0 = inductionVariable_0 + 1 | 0;
      if (i_0 === 0) {
        start[i_0] = diff[i_0];
      } else {
        start[i_0] = start[i_0 - 1 | 0] + diff[i_0] | 0;
      }
    }
     while (inductionVariable_0 <= last_0);
  this.c2_1 = start;
  var rangeLength = 'aaMBXHYH5BRpBPPPPPPRMP5BPPlCPPzBDOOPPcPXPzBvBjB3BOhDmBBpB7DoDYxB+EiBP1DoExBkBQhBekBPmBgBhBctBiBMWOOXhCsBpBkBUV3Ba4BkB0DlCgBXgBtD4FSdBfPhBPpKP0BvBXjEQ2CGsT8DhBtCqDpFvD1D3E0IrD2EkBJrBDOBsB+BPiBlB1EIjDPPPPPPPPPPPGPPMNLsBNPNPKCvBvBPPCkDPBmBPhDXXgD4B6FzEgDguG9vUtkB9JcuBSckEP/BPPPPPPBPf4FrBjEhBpC3B5BKaWPrBOwCk/KsCuLqDHPbPxPsFtEaaqDL';
  this.d2_1 = decodeVarLenBase64(rangeLength, fromBase64, 222);
  var rangeCategory = 'GFjgggUHGGFFZZZmzpz5qB6s6020B60ptltB6smt2sB60mz22B1+vv+8BZZ5s2850BW5q1ymtB506smzBF3q1q1qB1q1q1+Bgii4wDTm74g3KiggxqM60q1q1Bq1o1q1BF1qlrqrBZ2q5wprBGFZWWZGHFsjiooLowgmOowjkwCkgoiIk7ligGogiioBkwkiYkzj2oNoi+sbkwj04DghhkQ8wgiYkgoioDsgnkwC4gikQ//v+85BkwvoIsgoyI4yguI0whiwEowri4CoghsJowgqYowgm4DkwgsY/nwnzPowhmYkg6wI8yggZswikwHgxgmIoxgqYkwgk4DkxgmIkgoioBsgssoBgzgyI8g9gL8g9kI0wgwJoxgkoC0wgioFkw/wI0w53iF4gioYowjmgBHGq1qkgwBF1q1q8qBHwghuIwghyKk0goQkwgoQk3goQHGFHkyg0pBgxj6IoinkxDswno7Ikwhz9Bo0gioB8z48Rwli0xN0mpjoX8w78pDwltoqKHFGGwwgsIHFH3q1q16BFHWFZ1q10q1B2qlwq1B1q10q1B2q1yq1B6q1gq1Biq1qhxBir1qp1Bqt1q1qB1g1q1+B//3q16B///q1qBH/qlqq9Bholqq9B1i00a1q10qD1op1HkwmigEigiy6Cptogq1Bixo1kDq7/j00B2qgoBWGFm1lz50B6s5q1+BGWhggzhwBFFhgk4//Bo2jigE8wguI8wguI8wgugUog1qoB4qjmIwwi2KgkYHHH4lBgiFWkgIWoghssMmz5smrBZ3q1y50B5sm7gzBtz1smzB5smz50BqzqtmzB5sgzqzBF2/9//5BowgoIwmnkzPkwgk4C8ys65BkgoqI0wgy6FghquZo2giY0ghiIsgh24B4ghsQ8QF/v1q1OFs0O8iCHHF1qggz/B8wg6Iznv+//B08QgohsjK0QGFk7hsQ4gB';
  this.e2_1 = decodeVarLenBase64(rangeCategory, fromBase64, 222);
}
var Letter_instance;
function Letter_getInstance() {
  if (Letter_instance == null)
    new Letter();
  return Letter_instance;
}
function isWhitespaceImpl(_this__u8e3s4) {
  // Inline function 'kotlin.code' call
  var ch = Char__toInt_impl_vasixd(_this__u8e3s4);
  return (9 <= ch ? ch <= 13 : false) || (28 <= ch ? ch <= 32 : false) || ch === 160 || (ch > 4096 && (ch === 5760 || (8192 <= ch ? ch <= 8202 : false) || ch === 8232 || ch === 8233 || ch === 8239 || ch === 8287 || ch === 12288));
}
function Comparator() {
}
function isNaN_0(_this__u8e3s4) {
  return !(_this__u8e3s4 === _this__u8e3s4);
}
function takeHighestOneBit(_this__u8e3s4) {
  var tmp;
  if (_this__u8e3s4 === 0) {
    tmp = 0;
  } else {
    // Inline function 'kotlin.countLeadingZeroBits' call
    tmp = 1 << (31 - clz32(_this__u8e3s4) | 0);
  }
  return tmp;
}
function Unit() {
}
protoOf(Unit).toString = function () {
  return 'kotlin.Unit';
};
var Unit_instance;
function Unit_getInstance() {
  return Unit_instance;
}
function copyToArray(collection) {
  var tmp;
  // Inline function 'kotlin.js.asDynamic' call
  if (collection.toArray !== undefined) {
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    tmp = collection.toArray();
  } else {
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    tmp = collectionToArray(collection);
  }
  return tmp;
}
function checkIndexOverflow(index) {
  if (index < 0) {
    throwIndexOverflow();
  }
  return index;
}
function arrayCopy(source, destination, destinationOffset, startIndex, endIndex) {
  Companion_instance_5.g2(startIndex, endIndex, source.length);
  var rangeSize = endIndex - startIndex | 0;
  Companion_instance_5.g2(destinationOffset, destinationOffset + rangeSize | 0, destination.length);
  if (isView(destination) && isView(source)) {
    // Inline function 'kotlin.js.asDynamic' call
    var subrange = source.subarray(startIndex, endIndex);
    // Inline function 'kotlin.js.asDynamic' call
    destination.set(subrange, destinationOffset);
  } else {
    if (!(source === destination) || destinationOffset <= startIndex) {
      var inductionVariable = 0;
      if (inductionVariable < rangeSize)
        do {
          var index = inductionVariable;
          inductionVariable = inductionVariable + 1 | 0;
          destination[destinationOffset + index | 0] = source[startIndex + index | 0];
        }
         while (inductionVariable < rangeSize);
    } else {
      var inductionVariable_0 = rangeSize - 1 | 0;
      if (0 <= inductionVariable_0)
        do {
          var index_0 = inductionVariable_0;
          inductionVariable_0 = inductionVariable_0 + -1 | 0;
          destination[destinationOffset + index_0 | 0] = source[startIndex + index_0 | 0];
        }
         while (0 <= inductionVariable_0);
    }
  }
}
function terminateCollectionToArray(collectionSize, array) {
  return array;
}
function collectionToArray(collection) {
  return collectionToArrayCommonImpl(collection);
}
function arrayOfNulls(reference, size) {
  // Inline function 'kotlin.arrayOfNulls' call
  // Inline function 'kotlin.js.unsafeCast' call
  // Inline function 'kotlin.js.asDynamic' call
  return Array(size);
}
function setOf(element) {
  return hashSetOf([element]);
}
function listOf(element) {
  // Inline function 'kotlin.arrayOf' call
  // Inline function 'kotlin.js.unsafeCast' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp$ret$0 = [element];
  return new ArrayList(tmp$ret$0);
}
function mapCapacity(expectedSize) {
  return expectedSize;
}
function sort_0(_this__u8e3s4) {
  collectionsSort(_this__u8e3s4, naturalOrder());
}
function collectionsSort(list, comparator) {
  if (list.j() <= 1)
    return Unit_instance;
  var array = copyToArray(list);
  sortArrayWith(array, comparator);
  var inductionVariable = 0;
  var last = array.length;
  if (inductionVariable < last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      list.h2(i, array[i]);
    }
     while (inductionVariable < last);
}
function AbstractMutableCollection() {
  AbstractCollection.call(this);
}
protoOf(AbstractMutableCollection).p = function (elements) {
  this.i2();
  var modified = false;
  var _iterator__ex2g4s = elements.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    if (this.f(element))
      modified = true;
  }
  return modified;
};
protoOf(AbstractMutableCollection).toJSON = function () {
  return this.toArray();
};
protoOf(AbstractMutableCollection).i2 = function () {
};
function IteratorImpl($outer) {
  this.l2_1 = $outer;
  this.j2_1 = 0;
  this.k2_1 = -1;
}
protoOf(IteratorImpl).h = function () {
  return this.j2_1 < this.l2_1.j();
};
protoOf(IteratorImpl).i = function () {
  if (!this.h())
    throw NoSuchElementException_init_$Create$();
  var tmp = this;
  var _unary__edvuaz = this.j2_1;
  this.j2_1 = _unary__edvuaz + 1 | 0;
  tmp.k2_1 = _unary__edvuaz;
  return this.l2_1.k(this.k2_1);
};
function AbstractMutableList() {
  AbstractMutableCollection.call(this);
  this.m2_1 = 0;
}
protoOf(AbstractMutableList).f = function (element) {
  this.i2();
  this.n2(this.j(), element);
  return true;
};
protoOf(AbstractMutableList).g = function () {
  return new IteratorImpl(this);
};
protoOf(AbstractMutableList).h1 = function (element) {
  return this.o2(element) >= 0;
};
protoOf(AbstractMutableList).o2 = function (element) {
  var tmp$ret$0;
  $l$block: {
    // Inline function 'kotlin.collections.indexOfFirst' call
    var index = 0;
    var _iterator__ex2g4s = this.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      if (equals(item, element)) {
        tmp$ret$0 = index;
        break $l$block;
      }
      index = index + 1 | 0;
    }
    tmp$ret$0 = -1;
  }
  return tmp$ret$0;
};
protoOf(AbstractMutableList).equals = function (other) {
  if (other === this)
    return true;
  if (!(!(other == null) ? isInterface(other, KtList) : false))
    return false;
  return Companion_instance_5.p2(this, other);
};
protoOf(AbstractMutableList).hashCode = function () {
  return Companion_instance_5.q2(this);
};
function AbstractMutableMap() {
  AbstractMap.call(this);
  this.t2_1 = null;
  this.u2_1 = null;
}
protoOf(AbstractMutableMap).v2 = function () {
  return new HashMapValuesDefault(this);
};
protoOf(AbstractMutableMap).o1 = function () {
  var tmp0_elvis_lhs = this.u2_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.also' call
    var this_0 = this.v2();
    this.u2_1 = this_0;
    tmp = this_0;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(AbstractMutableMap).i2 = function () {
};
function AbstractMutableSet() {
  AbstractMutableCollection.call(this);
}
protoOf(AbstractMutableSet).equals = function (other) {
  if (other === this)
    return true;
  if (!(!(other == null) ? isInterface(other, KtSet) : false))
    return false;
  return Companion_instance_7.a3(this, other);
};
protoOf(AbstractMutableSet).hashCode = function () {
  return Companion_instance_7.b3(this);
};
function arrayOfUninitializedElements(capacity) {
  // Inline function 'kotlin.require' call
  if (!(capacity >= 0)) {
    var message = 'capacity must be non-negative.';
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  // Inline function 'kotlin.arrayOfNulls' call
  // Inline function 'kotlin.js.unsafeCast' call
  // Inline function 'kotlin.js.asDynamic' call
  return Array(capacity);
}
function resetRange(_this__u8e3s4, fromIndex, toIndex) {
  // Inline function 'kotlin.js.nativeFill' call
  // Inline function 'kotlin.js.asDynamic' call
  _this__u8e3s4.fill(null, fromIndex, toIndex);
}
function copyOfUninitializedElements(_this__u8e3s4, newSize) {
  // Inline function 'kotlin.js.unsafeCast' call
  // Inline function 'kotlin.js.asDynamic' call
  return copyOf_0(_this__u8e3s4, newSize);
}
function Companion_2() {
  Companion_instance_2 = this;
  var tmp = this;
  // Inline function 'kotlin.also' call
  var this_0 = ArrayList_init_$Create$_0(0);
  this_0.o_1 = true;
  tmp.c3_1 = this_0;
}
var Companion_instance_2;
function Companion_getInstance_2() {
  if (Companion_instance_2 == null)
    new Companion_2();
  return Companion_instance_2;
}
function ArrayList_init_$Init$($this) {
  // Inline function 'kotlin.emptyArray' call
  var tmp$ret$0 = [];
  ArrayList.call($this, tmp$ret$0);
  return $this;
}
function ArrayList_init_$Create$() {
  return ArrayList_init_$Init$(objectCreate(protoOf(ArrayList)));
}
function ArrayList_init_$Init$_0(initialCapacity, $this) {
  // Inline function 'kotlin.emptyArray' call
  var tmp$ret$0 = [];
  ArrayList.call($this, tmp$ret$0);
  // Inline function 'kotlin.require' call
  if (!(initialCapacity >= 0)) {
    var message = 'Negative initial capacity: ' + initialCapacity;
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  return $this;
}
function ArrayList_init_$Create$_0(initialCapacity) {
  return ArrayList_init_$Init$_0(initialCapacity, objectCreate(protoOf(ArrayList)));
}
function ArrayList_init_$Init$_1(elements, $this) {
  // Inline function 'kotlin.collections.toTypedArray' call
  var tmp$ret$0 = copyToArray(elements);
  ArrayList.call($this, tmp$ret$0);
  return $this;
}
function ArrayList_init_$Create$_1(elements) {
  return ArrayList_init_$Init$_1(elements, objectCreate(protoOf(ArrayList)));
}
function increaseLength($this, amount) {
  var previous = $this.j();
  // Inline function 'kotlin.js.asDynamic' call
  $this.n_1.length = $this.j() + amount | 0;
  return previous;
}
function rangeCheck($this, index) {
  // Inline function 'kotlin.apply' call
  Companion_instance_5.d3(index, $this.j());
  return index;
}
function insertionRangeCheck($this, index) {
  // Inline function 'kotlin.apply' call
  Companion_instance_5.e3(index, $this.j());
  return index;
}
function ArrayList(array) {
  Companion_getInstance_2();
  AbstractMutableList.call(this);
  this.n_1 = array;
  this.o_1 = false;
}
protoOf(ArrayList).j = function () {
  return this.n_1.length;
};
protoOf(ArrayList).k = function (index) {
  return this.n_1[rangeCheck(this, index)];
};
protoOf(ArrayList).h2 = function (index, element) {
  this.i2();
  rangeCheck(this, index);
  // Inline function 'kotlin.apply' call
  var this_0 = this.n_1[index];
  this.n_1[index] = element;
  return this_0;
};
protoOf(ArrayList).f = function (element) {
  this.i2();
  // Inline function 'kotlin.js.asDynamic' call
  this.n_1.push(element);
  this.m2_1 = this.m2_1 + 1 | 0;
  return true;
};
protoOf(ArrayList).n2 = function (index, element) {
  this.i2();
  // Inline function 'kotlin.js.asDynamic' call
  this.n_1.splice(insertionRangeCheck(this, index), 0, element);
  this.m2_1 = this.m2_1 + 1 | 0;
};
protoOf(ArrayList).p = function (elements) {
  this.i2();
  if (elements.l())
    return false;
  var offset = increaseLength(this, elements.j());
  // Inline function 'kotlin.collections.forEachIndexed' call
  var index = 0;
  var _iterator__ex2g4s = elements.g();
  while (_iterator__ex2g4s.h()) {
    var item = _iterator__ex2g4s.i();
    var _unary__edvuaz = index;
    index = _unary__edvuaz + 1 | 0;
    var index_0 = checkIndexOverflow(_unary__edvuaz);
    this.n_1[offset + index_0 | 0] = item;
  }
  this.m2_1 = this.m2_1 + 1 | 0;
  return true;
};
protoOf(ArrayList).o2 = function (element) {
  return indexOf(this.n_1, element);
};
protoOf(ArrayList).toString = function () {
  return arrayToString(this.n_1);
};
protoOf(ArrayList).f3 = function () {
  return [].slice.call(this.n_1);
};
protoOf(ArrayList).toArray = function () {
  return this.f3();
};
protoOf(ArrayList).i2 = function () {
  if (this.o_1)
    throw UnsupportedOperationException_init_$Create$();
};
var _stableSortingIsSupported;
function sortArrayWith(array, comparator) {
  if (getStableSortingIsSupported()) {
    var comparison = sortArrayWith$lambda(comparator);
    // Inline function 'kotlin.js.asDynamic' call
    array.sort(comparison);
  } else {
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    mergeSort(array, 0, get_lastIndex(array), comparator);
  }
}
function sortArray(array) {
  if (getStableSortingIsSupported()) {
    var comparison = sortArray$lambda;
    // Inline function 'kotlin.js.asDynamic' call
    array.sort(comparison);
  } else {
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    mergeSort(array, 0, get_lastIndex(array), naturalOrder());
  }
}
function getStableSortingIsSupported() {
  var tmp0_safe_receiver = _stableSortingIsSupported;
  if (tmp0_safe_receiver == null)
    null;
  else {
    // Inline function 'kotlin.let' call
    return tmp0_safe_receiver;
  }
  _stableSortingIsSupported = false;
  // Inline function 'kotlin.js.unsafeCast' call
  var array = [];
  var inductionVariable = 0;
  if (inductionVariable < 600)
    do {
      var index = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      // Inline function 'kotlin.js.asDynamic' call
      array.push(index);
    }
     while (inductionVariable < 600);
  var comparison = getStableSortingIsSupported$lambda;
  // Inline function 'kotlin.js.asDynamic' call
  array.sort(comparison);
  var inductionVariable_0 = 1;
  var last = array.length;
  if (inductionVariable_0 < last)
    do {
      var index_0 = inductionVariable_0;
      inductionVariable_0 = inductionVariable_0 + 1 | 0;
      var a = array[index_0 - 1 | 0];
      var b = array[index_0];
      if ((a & 3) === (b & 3) && a >= b)
        return false;
    }
     while (inductionVariable_0 < last);
  _stableSortingIsSupported = true;
  return true;
}
function mergeSort(array, start, endInclusive, comparator) {
  // Inline function 'kotlin.arrayOfNulls' call
  var size = array.length;
  // Inline function 'kotlin.js.unsafeCast' call
  // Inline function 'kotlin.js.asDynamic' call
  var buffer = Array(size);
  var result = mergeSort_0(array, buffer, start, endInclusive, comparator);
  if (!(result === array)) {
    var inductionVariable = start;
    if (inductionVariable <= endInclusive)
      do {
        var i = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        array[i] = result[i];
      }
       while (!(i === endInclusive));
  }
}
function mergeSort_0(array, buffer, start, end, comparator) {
  if (start === end) {
    return array;
  }
  var median = (start + end | 0) / 2 | 0;
  var left = mergeSort_0(array, buffer, start, median, comparator);
  var right = mergeSort_0(array, buffer, median + 1 | 0, end, comparator);
  var target = left === buffer ? array : buffer;
  var leftIndex = start;
  var rightIndex = median + 1 | 0;
  var inductionVariable = start;
  if (inductionVariable <= end)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      if (leftIndex <= median && rightIndex <= end) {
        var leftValue = left[leftIndex];
        var rightValue = right[rightIndex];
        if (comparator.compare(leftValue, rightValue) <= 0) {
          target[i] = leftValue;
          leftIndex = leftIndex + 1 | 0;
        } else {
          target[i] = rightValue;
          rightIndex = rightIndex + 1 | 0;
        }
      } else if (leftIndex <= median) {
        target[i] = left[leftIndex];
        leftIndex = leftIndex + 1 | 0;
      } else {
        target[i] = right[rightIndex];
        rightIndex = rightIndex + 1 | 0;
      }
    }
     while (!(i === end));
  return target;
}
function sortArrayWith$lambda($comparator) {
  return function (a, b) {
    return $comparator.compare(a, b);
  };
}
function sortArray$lambda(a, b) {
  return compareTo(a, b);
}
function getStableSortingIsSupported$lambda(a, b) {
  return (a & 3) - (b & 3) | 0;
}
function HashMap_init_$Init$(internalMap, $this) {
  AbstractMutableMap.call($this);
  HashMap.call($this);
  $this.k3_1 = internalMap;
  return $this;
}
function HashMap_init_$Init$_0($this) {
  HashMap_init_$Init$(InternalHashMap_init_$Create$(), $this);
  return $this;
}
function HashMap_init_$Create$() {
  return HashMap_init_$Init$_0(objectCreate(protoOf(HashMap)));
}
function HashMap_init_$Init$_1(initialCapacity, loadFactor, $this) {
  HashMap_init_$Init$(InternalHashMap_init_$Create$_1(initialCapacity, loadFactor), $this);
  return $this;
}
function HashMap_init_$Init$_2(initialCapacity, $this) {
  HashMap_init_$Init$_1(initialCapacity, 1.0, $this);
  return $this;
}
protoOf(HashMap).l1 = function (key) {
  return this.k3_1.m3(key);
};
protoOf(HashMap).m1 = function (value) {
  return this.k3_1.m1(value);
};
protoOf(HashMap).v2 = function () {
  return new HashMapValues(this.k3_1);
};
protoOf(HashMap).p1 = function () {
  var tmp0_elvis_lhs = this.l3_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.also' call
    var this_0 = new HashMapEntrySet(this.k3_1);
    this.l3_1 = this_0;
    tmp = this_0;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(HashMap).n1 = function (key) {
  return this.k3_1.n1(key);
};
protoOf(HashMap).w2 = function (key, value) {
  return this.k3_1.w2(key, value);
};
protoOf(HashMap).j = function () {
  return this.k3_1.j();
};
function HashMap() {
  this.l3_1 = null;
}
function HashMapValues(backing) {
  AbstractMutableCollection.call(this);
  this.n3_1 = backing;
}
protoOf(HashMapValues).j = function () {
  return this.n3_1.j();
};
protoOf(HashMapValues).l = function () {
  return this.n3_1.j() === 0;
};
protoOf(HashMapValues).o3 = function (element) {
  return this.n3_1.m1(element);
};
protoOf(HashMapValues).h1 = function (element) {
  if (!true)
    return false;
  return this.o3(element);
};
protoOf(HashMapValues).p3 = function (element) {
  throw UnsupportedOperationException_init_$Create$();
};
protoOf(HashMapValues).f = function (element) {
  return this.p3(element);
};
protoOf(HashMapValues).q3 = function (elements) {
  throw UnsupportedOperationException_init_$Create$();
};
protoOf(HashMapValues).p = function (elements) {
  return this.q3(elements);
};
protoOf(HashMapValues).g = function () {
  return this.n3_1.r3();
};
protoOf(HashMapValues).i2 = function () {
  return this.n3_1.s3();
};
function HashMapEntrySet(backing) {
  HashMapEntrySetBase.call(this, backing);
}
protoOf(HashMapEntrySet).g = function () {
  return this.u3_1.v3();
};
function HashMapEntrySetBase(backing) {
  AbstractMutableSet.call(this);
  this.u3_1 = backing;
}
protoOf(HashMapEntrySetBase).j = function () {
  return this.u3_1.j();
};
protoOf(HashMapEntrySetBase).l = function () {
  return this.u3_1.j() === 0;
};
protoOf(HashMapEntrySetBase).w3 = function (element) {
  return this.u3_1.y3(element);
};
protoOf(HashMapEntrySetBase).h1 = function (element) {
  if (!(!(element == null) ? isInterface(element, Entry) : false))
    return false;
  return this.w3((!(element == null) ? isInterface(element, Entry) : false) ? element : THROW_CCE());
};
protoOf(HashMapEntrySetBase).x3 = function (element) {
  throw UnsupportedOperationException_init_$Create$();
};
protoOf(HashMapEntrySetBase).f = function (element) {
  return this.x3((!(element == null) ? isInterface(element, Entry) : false) ? element : THROW_CCE());
};
protoOf(HashMapEntrySetBase).p = function (elements) {
  throw UnsupportedOperationException_init_$Create$();
};
protoOf(HashMapEntrySetBase).i1 = function (elements) {
  return this.u3_1.z3(elements);
};
protoOf(HashMapEntrySetBase).i2 = function () {
  return this.u3_1.s3();
};
function HashMapValuesDefault$iterator$1($entryIterator) {
  this.a4_1 = $entryIterator;
}
protoOf(HashMapValuesDefault$iterator$1).h = function () {
  return this.a4_1.h();
};
protoOf(HashMapValuesDefault$iterator$1).i = function () {
  return this.a4_1.i().k1();
};
function HashMapValuesDefault(backingMap) {
  AbstractMutableCollection.call(this);
  this.b4_1 = backingMap;
}
protoOf(HashMapValuesDefault).p3 = function (element) {
  throw UnsupportedOperationException_init_$Create$_0('Add is not supported on values');
};
protoOf(HashMapValuesDefault).f = function (element) {
  return this.p3(element);
};
protoOf(HashMapValuesDefault).o3 = function (element) {
  return this.b4_1.m1(element);
};
protoOf(HashMapValuesDefault).h1 = function (element) {
  if (!true)
    return false;
  return this.o3(element);
};
protoOf(HashMapValuesDefault).g = function () {
  var entryIterator = this.b4_1.p1().g();
  return new HashMapValuesDefault$iterator$1(entryIterator);
};
protoOf(HashMapValuesDefault).j = function () {
  return this.b4_1.j();
};
protoOf(HashMapValuesDefault).i2 = function () {
  return this.b4_1.i2();
};
function HashSet_init_$Init$(map, $this) {
  AbstractMutableSet.call($this);
  HashSet.call($this);
  $this.c4_1 = map;
  return $this;
}
function HashSet_init_$Init$_0($this) {
  HashSet_init_$Init$(InternalHashMap_init_$Create$(), $this);
  return $this;
}
function HashSet_init_$Create$() {
  return HashSet_init_$Init$_0(objectCreate(protoOf(HashSet)));
}
function HashSet_init_$Init$_1(elements, $this) {
  HashSet_init_$Init$(InternalHashMap_init_$Create$_0(elements.j()), $this);
  var _iterator__ex2g4s = elements.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    $this.c4_1.w2(element, true);
  }
  return $this;
}
function HashSet_init_$Init$_2(initialCapacity, loadFactor, $this) {
  HashSet_init_$Init$(InternalHashMap_init_$Create$_1(initialCapacity, loadFactor), $this);
  return $this;
}
function HashSet_init_$Init$_3(initialCapacity, $this) {
  HashSet_init_$Init$_2(initialCapacity, 1.0, $this);
  return $this;
}
function HashSet_init_$Create$_0(initialCapacity) {
  return HashSet_init_$Init$_3(initialCapacity, objectCreate(protoOf(HashSet)));
}
protoOf(HashSet).f = function (element) {
  return this.c4_1.w2(element, true) == null;
};
protoOf(HashSet).h1 = function (element) {
  return this.c4_1.m3(element);
};
protoOf(HashSet).l = function () {
  return this.c4_1.j() === 0;
};
protoOf(HashSet).g = function () {
  return this.c4_1.d4();
};
protoOf(HashSet).j = function () {
  return this.c4_1.j();
};
function HashSet() {
}
function computeHashSize($this, capacity) {
  return takeHighestOneBit(imul_0(coerceAtLeast(capacity, 1), 3));
}
function computeShift($this, hashSize) {
  // Inline function 'kotlin.countLeadingZeroBits' call
  return clz32(hashSize) + 1 | 0;
}
function checkForComodification($this) {
  if (!($this.o4_1.l4_1 === $this.q4_1))
    throw ConcurrentModificationException_init_$Create$_0('The backing map has been modified after this entry was obtained.');
}
function InternalHashMap_init_$Init$($this) {
  InternalHashMap_init_$Init$_0(8, $this);
  return $this;
}
function InternalHashMap_init_$Create$() {
  return InternalHashMap_init_$Init$(objectCreate(protoOf(InternalHashMap)));
}
function InternalHashMap_init_$Init$_0(initialCapacity, $this) {
  InternalHashMap.call($this, arrayOfUninitializedElements(initialCapacity), null, new Int32Array(initialCapacity), new Int32Array(computeHashSize(Companion_instance_3, initialCapacity)), 2, 0);
  return $this;
}
function InternalHashMap_init_$Create$_0(initialCapacity) {
  return InternalHashMap_init_$Init$_0(initialCapacity, objectCreate(protoOf(InternalHashMap)));
}
function InternalHashMap_init_$Init$_1(initialCapacity, loadFactor, $this) {
  InternalHashMap_init_$Init$_0(initialCapacity, $this);
  // Inline function 'kotlin.require' call
  if (!(loadFactor > 0)) {
    var message = 'Non-positive load factor: ' + loadFactor;
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  return $this;
}
function InternalHashMap_init_$Create$_1(initialCapacity, loadFactor) {
  return InternalHashMap_init_$Init$_1(initialCapacity, loadFactor, objectCreate(protoOf(InternalHashMap)));
}
function _get_capacity__a9k9f3($this) {
  return $this.e4_1.length;
}
function _get_hashSize__tftcho($this) {
  return $this.h4_1.length;
}
function registerModification($this) {
  $this.l4_1 = $this.l4_1 + 1 | 0;
}
function ensureExtraCapacity($this, n) {
  if (shouldCompact($this, n)) {
    compact($this, true);
  } else {
    ensureCapacity($this, $this.j4_1 + n | 0);
  }
}
function shouldCompact($this, extraCapacity) {
  var spareCapacity = _get_capacity__a9k9f3($this) - $this.j4_1 | 0;
  var gaps = $this.j4_1 - $this.j() | 0;
  return spareCapacity < extraCapacity && (gaps + spareCapacity | 0) >= extraCapacity && gaps >= (_get_capacity__a9k9f3($this) / 4 | 0);
}
function ensureCapacity($this, minCapacity) {
  if (minCapacity < 0)
    throw RuntimeException_init_$Create$_0('too many elements');
  if (minCapacity > _get_capacity__a9k9f3($this)) {
    var newSize = Companion_instance_5.r4(_get_capacity__a9k9f3($this), minCapacity);
    $this.e4_1 = copyOfUninitializedElements($this.e4_1, newSize);
    var tmp = $this;
    var tmp0_safe_receiver = $this.f4_1;
    tmp.f4_1 = tmp0_safe_receiver == null ? null : copyOfUninitializedElements(tmp0_safe_receiver, newSize);
    $this.g4_1 = copyOf($this.g4_1, newSize);
    var newHashSize = computeHashSize(Companion_instance_3, newSize);
    if (newHashSize > _get_hashSize__tftcho($this)) {
      rehash($this, newHashSize);
    }
  }
}
function allocateValuesArray($this) {
  var curValuesArray = $this.f4_1;
  if (!(curValuesArray == null))
    return curValuesArray;
  var newValuesArray = arrayOfUninitializedElements(_get_capacity__a9k9f3($this));
  $this.f4_1 = newValuesArray;
  return newValuesArray;
}
function hash($this, key) {
  return key == null ? 0 : imul_0(hashCode_0(key), -1640531527) >>> $this.k4_1 | 0;
}
function compact($this, updateHashArray) {
  var i = 0;
  var j = 0;
  var valuesArray = $this.f4_1;
  while (i < $this.j4_1) {
    var hash = $this.g4_1[i];
    if (hash >= 0) {
      $this.e4_1[j] = $this.e4_1[i];
      if (!(valuesArray == null)) {
        valuesArray[j] = valuesArray[i];
      }
      if (updateHashArray) {
        $this.g4_1[j] = hash;
        $this.h4_1[hash] = j + 1 | 0;
      }
      j = j + 1 | 0;
    }
    i = i + 1 | 0;
  }
  resetRange($this.e4_1, j, $this.j4_1);
  if (valuesArray == null)
    null;
  else {
    resetRange(valuesArray, j, $this.j4_1);
  }
  $this.j4_1 = j;
}
function rehash($this, newHashSize) {
  registerModification($this);
  if ($this.j4_1 > $this.m4_1) {
    compact($this, false);
  }
  $this.h4_1 = new Int32Array(newHashSize);
  $this.k4_1 = computeShift(Companion_instance_3, newHashSize);
  var i = 0;
  while (i < $this.j4_1) {
    var _unary__edvuaz = i;
    i = _unary__edvuaz + 1 | 0;
    if (!putRehash($this, _unary__edvuaz)) {
      throw IllegalStateException_init_$Create$_0('This cannot happen with fixed magic multiplier and grow-only hash array. Have object hashCodes changed?');
    }
  }
}
function putRehash($this, i) {
  var hash_0 = hash($this, $this.e4_1[i]);
  var probesLeft = $this.i4_1;
  while (true) {
    var index = $this.h4_1[hash_0];
    if (index === 0) {
      $this.h4_1[hash_0] = i + 1 | 0;
      $this.g4_1[i] = hash_0;
      return true;
    }
    probesLeft = probesLeft - 1 | 0;
    if (probesLeft < 0)
      return false;
    var _unary__edvuaz = hash_0;
    hash_0 = _unary__edvuaz - 1 | 0;
    if (_unary__edvuaz === 0)
      hash_0 = _get_hashSize__tftcho($this) - 1 | 0;
  }
}
function findKey($this, key) {
  var hash_0 = hash($this, key);
  var probesLeft = $this.i4_1;
  while (true) {
    var index = $this.h4_1[hash_0];
    if (index === 0)
      return -1;
    if (equals($this.e4_1[index - 1 | 0], key))
      return index - 1 | 0;
    probesLeft = probesLeft - 1 | 0;
    if (probesLeft < 0)
      return -1;
    var _unary__edvuaz = hash_0;
    hash_0 = _unary__edvuaz - 1 | 0;
    if (_unary__edvuaz === 0)
      hash_0 = _get_hashSize__tftcho($this) - 1 | 0;
  }
}
function findValue($this, value) {
  var i = $this.j4_1;
  $l$loop: while (true) {
    i = i - 1 | 0;
    if (!(i >= 0)) {
      break $l$loop;
    }
    if ($this.g4_1[i] >= 0 && equals(ensureNotNull($this.f4_1)[i], value))
      return i;
  }
  return -1;
}
function addKey($this, key) {
  $this.s3();
  retry: while (true) {
    var hash_0 = hash($this, key);
    var tentativeMaxProbeDistance = coerceAtMost(imul_0($this.i4_1, 2), _get_hashSize__tftcho($this) / 2 | 0);
    var probeDistance = 0;
    while (true) {
      var index = $this.h4_1[hash_0];
      if (index === 0) {
        if ($this.j4_1 >= _get_capacity__a9k9f3($this)) {
          ensureExtraCapacity($this, 1);
          continue retry;
        }
        var _unary__edvuaz = $this.j4_1;
        $this.j4_1 = _unary__edvuaz + 1 | 0;
        var putIndex = _unary__edvuaz;
        $this.e4_1[putIndex] = key;
        $this.g4_1[putIndex] = hash_0;
        $this.h4_1[hash_0] = putIndex + 1 | 0;
        $this.m4_1 = $this.m4_1 + 1 | 0;
        registerModification($this);
        if (probeDistance > $this.i4_1)
          $this.i4_1 = probeDistance;
        return putIndex;
      }
      if (equals($this.e4_1[index - 1 | 0], key)) {
        return -index | 0;
      }
      probeDistance = probeDistance + 1 | 0;
      if (probeDistance > tentativeMaxProbeDistance) {
        rehash($this, imul_0(_get_hashSize__tftcho($this), 2));
        continue retry;
      }
      var _unary__edvuaz_0 = hash_0;
      hash_0 = _unary__edvuaz_0 - 1 | 0;
      if (_unary__edvuaz_0 === 0)
        hash_0 = _get_hashSize__tftcho($this) - 1 | 0;
    }
  }
}
function contentEquals_0($this, other) {
  return $this.m4_1 === other.j() && $this.z3(other.p1());
}
function Companion_3() {
  this.s4_1 = -1640531527;
  this.t4_1 = 8;
  this.u4_1 = 2;
  this.v4_1 = -1;
}
var Companion_instance_3;
function Companion_getInstance_3() {
  return Companion_instance_3;
}
function Itr(map) {
  this.w4_1 = map;
  this.x4_1 = 0;
  this.y4_1 = -1;
  this.z4_1 = this.w4_1.l4_1;
  this.a5();
}
protoOf(Itr).a5 = function () {
  while (this.x4_1 < this.w4_1.j4_1 && this.w4_1.g4_1[this.x4_1] < 0) {
    this.x4_1 = this.x4_1 + 1 | 0;
  }
};
protoOf(Itr).h = function () {
  return this.x4_1 < this.w4_1.j4_1;
};
protoOf(Itr).b5 = function () {
  if (!(this.w4_1.l4_1 === this.z4_1))
    throw ConcurrentModificationException_init_$Create$();
};
function KeysItr(map) {
  Itr.call(this, map);
}
protoOf(KeysItr).i = function () {
  this.b5();
  if (this.x4_1 >= this.w4_1.j4_1)
    throw NoSuchElementException_init_$Create$();
  var tmp = this;
  var _unary__edvuaz = this.x4_1;
  this.x4_1 = _unary__edvuaz + 1 | 0;
  tmp.y4_1 = _unary__edvuaz;
  var result = this.w4_1.e4_1[this.y4_1];
  this.a5();
  return result;
};
function ValuesItr(map) {
  Itr.call(this, map);
}
protoOf(ValuesItr).i = function () {
  this.b5();
  if (this.x4_1 >= this.w4_1.j4_1)
    throw NoSuchElementException_init_$Create$();
  var tmp = this;
  var _unary__edvuaz = this.x4_1;
  this.x4_1 = _unary__edvuaz + 1 | 0;
  tmp.y4_1 = _unary__edvuaz;
  var result = ensureNotNull(this.w4_1.f4_1)[this.y4_1];
  this.a5();
  return result;
};
function EntriesItr(map) {
  Itr.call(this, map);
}
protoOf(EntriesItr).i = function () {
  this.b5();
  if (this.x4_1 >= this.w4_1.j4_1)
    throw NoSuchElementException_init_$Create$();
  var tmp = this;
  var _unary__edvuaz = this.x4_1;
  this.x4_1 = _unary__edvuaz + 1 | 0;
  tmp.y4_1 = _unary__edvuaz;
  var result = new EntryRef(this.w4_1, this.y4_1);
  this.a5();
  return result;
};
protoOf(EntriesItr).o5 = function () {
  if (this.x4_1 >= this.w4_1.j4_1)
    throw NoSuchElementException_init_$Create$();
  var tmp = this;
  var _unary__edvuaz = this.x4_1;
  this.x4_1 = _unary__edvuaz + 1 | 0;
  tmp.y4_1 = _unary__edvuaz;
  // Inline function 'kotlin.hashCode' call
  var tmp0_safe_receiver = this.w4_1.e4_1[this.y4_1];
  var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : hashCode_0(tmp0_safe_receiver);
  var tmp_0 = tmp1_elvis_lhs == null ? 0 : tmp1_elvis_lhs;
  // Inline function 'kotlin.hashCode' call
  var tmp0_safe_receiver_0 = ensureNotNull(this.w4_1.f4_1)[this.y4_1];
  var tmp1_elvis_lhs_0 = tmp0_safe_receiver_0 == null ? null : hashCode_0(tmp0_safe_receiver_0);
  var result = tmp_0 ^ (tmp1_elvis_lhs_0 == null ? 0 : tmp1_elvis_lhs_0);
  this.a5();
  return result;
};
protoOf(EntriesItr).p5 = function (sb) {
  if (this.x4_1 >= this.w4_1.j4_1)
    throw NoSuchElementException_init_$Create$();
  var tmp = this;
  var _unary__edvuaz = this.x4_1;
  this.x4_1 = _unary__edvuaz + 1 | 0;
  tmp.y4_1 = _unary__edvuaz;
  var key = this.w4_1.e4_1[this.y4_1];
  if (equals(key, this.w4_1))
    sb.s5('(this Map)');
  else
    sb.r5(key);
  sb.t5(_Char___init__impl__6a9atx(61));
  var value = ensureNotNull(this.w4_1.f4_1)[this.y4_1];
  if (equals(value, this.w4_1))
    sb.s5('(this Map)');
  else
    sb.r5(value);
  this.a5();
};
function EntryRef(map, index) {
  this.o4_1 = map;
  this.p4_1 = index;
  this.q4_1 = this.o4_1.l4_1;
}
protoOf(EntryRef).j1 = function () {
  checkForComodification(this);
  return this.o4_1.e4_1[this.p4_1];
};
protoOf(EntryRef).k1 = function () {
  checkForComodification(this);
  return ensureNotNull(this.o4_1.f4_1)[this.p4_1];
};
protoOf(EntryRef).equals = function (other) {
  var tmp;
  var tmp_0;
  if (!(other == null) ? isInterface(other, Entry) : false) {
    tmp_0 = equals(other.j1(), this.j1());
  } else {
    tmp_0 = false;
  }
  if (tmp_0) {
    tmp = equals(other.k1(), this.k1());
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(EntryRef).hashCode = function () {
  // Inline function 'kotlin.hashCode' call
  var tmp0_safe_receiver = this.j1();
  var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : hashCode_0(tmp0_safe_receiver);
  var tmp = tmp1_elvis_lhs == null ? 0 : tmp1_elvis_lhs;
  // Inline function 'kotlin.hashCode' call
  var tmp0_safe_receiver_0 = this.k1();
  var tmp1_elvis_lhs_0 = tmp0_safe_receiver_0 == null ? null : hashCode_0(tmp0_safe_receiver_0);
  return tmp ^ (tmp1_elvis_lhs_0 == null ? 0 : tmp1_elvis_lhs_0);
};
protoOf(EntryRef).toString = function () {
  return toString_0(this.j1()) + '=' + toString_0(this.k1());
};
function InternalHashMap(keysArray, valuesArray, presenceArray, hashArray, maxProbeDistance, length) {
  this.e4_1 = keysArray;
  this.f4_1 = valuesArray;
  this.g4_1 = presenceArray;
  this.h4_1 = hashArray;
  this.i4_1 = maxProbeDistance;
  this.j4_1 = length;
  this.k4_1 = computeShift(Companion_instance_3, _get_hashSize__tftcho(this));
  this.l4_1 = 0;
  this.m4_1 = 0;
  this.n4_1 = false;
}
protoOf(InternalHashMap).j = function () {
  return this.m4_1;
};
protoOf(InternalHashMap).u5 = function () {
  this.s3();
  this.n4_1 = true;
};
protoOf(InternalHashMap).m1 = function (value) {
  return findValue(this, value) >= 0;
};
protoOf(InternalHashMap).n1 = function (key) {
  var index = findKey(this, key);
  if (index < 0)
    return null;
  return ensureNotNull(this.f4_1)[index];
};
protoOf(InternalHashMap).m3 = function (key) {
  return findKey(this, key) >= 0;
};
protoOf(InternalHashMap).w2 = function (key, value) {
  var index = addKey(this, key);
  var valuesArray = allocateValuesArray(this);
  if (index < 0) {
    var oldValue = valuesArray[(-index | 0) - 1 | 0];
    valuesArray[(-index | 0) - 1 | 0] = value;
    return oldValue;
  } else {
    valuesArray[index] = value;
    return null;
  }
};
protoOf(InternalHashMap).equals = function (other) {
  var tmp;
  if (other === this) {
    tmp = true;
  } else {
    var tmp_0;
    if (!(other == null) ? isInterface(other, KtMap) : false) {
      tmp_0 = contentEquals_0(this, other);
    } else {
      tmp_0 = false;
    }
    tmp = tmp_0;
  }
  return tmp;
};
protoOf(InternalHashMap).hashCode = function () {
  var result = 0;
  var it = this.v3();
  while (it.h()) {
    result = result + it.o5() | 0;
  }
  return result;
};
protoOf(InternalHashMap).toString = function () {
  var sb = StringBuilder_init_$Create$(2 + imul_0(this.m4_1, 3) | 0);
  sb.s5('{');
  var i = 0;
  var it = this.v3();
  while (it.h()) {
    if (i > 0) {
      sb.s5(', ');
    }
    it.p5(sb);
    i = i + 1 | 0;
  }
  sb.s5('}');
  return sb.toString();
};
protoOf(InternalHashMap).s3 = function () {
  if (this.n4_1)
    throw UnsupportedOperationException_init_$Create$();
};
protoOf(InternalHashMap).y3 = function (entry) {
  var index = findKey(this, entry.j1());
  if (index < 0)
    return false;
  return equals(ensureNotNull(this.f4_1)[index], entry.k1());
};
protoOf(InternalHashMap).v5 = function (entry) {
  return this.y3(isInterface(entry, Entry) ? entry : THROW_CCE());
};
protoOf(InternalHashMap).d4 = function () {
  return new KeysItr(this);
};
protoOf(InternalHashMap).r3 = function () {
  return new ValuesItr(this);
};
protoOf(InternalHashMap).v3 = function () {
  return new EntriesItr(this);
};
function InternalMap() {
}
function LinkedHashMap_init_$Init$($this) {
  HashMap_init_$Init$_0($this);
  LinkedHashMap.call($this);
  return $this;
}
function LinkedHashMap_init_$Create$() {
  return LinkedHashMap_init_$Init$(objectCreate(protoOf(LinkedHashMap)));
}
function LinkedHashMap_init_$Init$_0(initialCapacity, $this) {
  HashMap_init_$Init$_2(initialCapacity, $this);
  LinkedHashMap.call($this);
  return $this;
}
function LinkedHashMap_init_$Create$_0(initialCapacity) {
  return LinkedHashMap_init_$Init$_0(initialCapacity, objectCreate(protoOf(LinkedHashMap)));
}
function LinkedHashMap_init_$Init$_1(internalMap, $this) {
  HashMap_init_$Init$(internalMap, $this);
  LinkedHashMap.call($this);
  return $this;
}
function LinkedHashMap_init_$Create$_1(internalMap) {
  return LinkedHashMap_init_$Init$_1(internalMap, objectCreate(protoOf(LinkedHashMap)));
}
function EmptyHolder() {
  EmptyHolder_instance = this;
  var tmp = this;
  // Inline function 'kotlin.also' call
  var this_0 = InternalHashMap_init_$Create$_0(0);
  this_0.u5();
  tmp.w5_1 = LinkedHashMap_init_$Create$_1(this_0);
}
var EmptyHolder_instance;
function EmptyHolder_getInstance() {
  if (EmptyHolder_instance == null)
    new EmptyHolder();
  return EmptyHolder_instance;
}
protoOf(LinkedHashMap).d6 = function () {
  this.k3_1.u5();
  var tmp;
  if (this.j() > 0) {
    tmp = this;
  } else {
    // Inline function 'kotlin.js.unsafeCast' call
    // Inline function 'kotlin.js.asDynamic' call
    tmp = EmptyHolder_getInstance().w5_1;
  }
  return tmp;
};
protoOf(LinkedHashMap).i2 = function () {
  return this.k3_1.s3();
};
function LinkedHashMap() {
}
function LinkedHashSet_init_$Init$($this) {
  HashSet_init_$Init$_0($this);
  LinkedHashSet.call($this);
  return $this;
}
function LinkedHashSet_init_$Create$() {
  return LinkedHashSet_init_$Init$(objectCreate(protoOf(LinkedHashSet)));
}
function LinkedHashSet_init_$Init$_0(elements, $this) {
  HashSet_init_$Init$_1(elements, $this);
  LinkedHashSet.call($this);
  return $this;
}
function LinkedHashSet_init_$Create$_0(elements) {
  return LinkedHashSet_init_$Init$_0(elements, objectCreate(protoOf(LinkedHashSet)));
}
function LinkedHashSet_init_$Init$_1(initialCapacity, loadFactor, $this) {
  HashSet_init_$Init$_2(initialCapacity, loadFactor, $this);
  LinkedHashSet.call($this);
  return $this;
}
function LinkedHashSet_init_$Init$_2(initialCapacity, $this) {
  LinkedHashSet_init_$Init$_1(initialCapacity, 1.0, $this);
  return $this;
}
function LinkedHashSet_init_$Create$_1(initialCapacity) {
  return LinkedHashSet_init_$Init$_2(initialCapacity, objectCreate(protoOf(LinkedHashSet)));
}
protoOf(LinkedHashSet).i2 = function () {
  return this.c4_1.s3();
};
function LinkedHashSet() {
}
function UnsupportedOperationException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  UnsupportedOperationException.call($this);
  return $this;
}
function UnsupportedOperationException_init_$Create$() {
  var tmp = UnsupportedOperationException_init_$Init$(objectCreate(protoOf(UnsupportedOperationException)));
  captureStack(tmp, UnsupportedOperationException_init_$Create$);
  return tmp;
}
function UnsupportedOperationException_init_$Init$_0(message, $this) {
  RuntimeException_init_$Init$_0(message, $this);
  UnsupportedOperationException.call($this);
  return $this;
}
function UnsupportedOperationException_init_$Create$_0(message) {
  var tmp = UnsupportedOperationException_init_$Init$_0(message, objectCreate(protoOf(UnsupportedOperationException)));
  captureStack(tmp, UnsupportedOperationException_init_$Create$_0);
  return tmp;
}
function UnsupportedOperationException() {
  captureStack(this, UnsupportedOperationException);
}
function IllegalArgumentException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  IllegalArgumentException.call($this);
  return $this;
}
function IllegalArgumentException_init_$Create$() {
  var tmp = IllegalArgumentException_init_$Init$(objectCreate(protoOf(IllegalArgumentException)));
  captureStack(tmp, IllegalArgumentException_init_$Create$);
  return tmp;
}
function IllegalArgumentException_init_$Init$_0(message, $this) {
  RuntimeException_init_$Init$_0(message, $this);
  IllegalArgumentException.call($this);
  return $this;
}
function IllegalArgumentException_init_$Create$_0(message) {
  var tmp = IllegalArgumentException_init_$Init$_0(message, objectCreate(protoOf(IllegalArgumentException)));
  captureStack(tmp, IllegalArgumentException_init_$Create$_0);
  return tmp;
}
function IllegalArgumentException_init_$Init$_1(message, cause, $this) {
  RuntimeException_init_$Init$_1(message, cause, $this);
  IllegalArgumentException.call($this);
  return $this;
}
function IllegalArgumentException_init_$Create$_1(message, cause) {
  var tmp = IllegalArgumentException_init_$Init$_1(message, cause, objectCreate(protoOf(IllegalArgumentException)));
  captureStack(tmp, IllegalArgumentException_init_$Create$_1);
  return tmp;
}
function IllegalArgumentException() {
  captureStack(this, IllegalArgumentException);
}
function RuntimeException_init_$Init$($this) {
  Exception_init_$Init$($this);
  RuntimeException.call($this);
  return $this;
}
function RuntimeException_init_$Create$() {
  var tmp = RuntimeException_init_$Init$(objectCreate(protoOf(RuntimeException)));
  captureStack(tmp, RuntimeException_init_$Create$);
  return tmp;
}
function RuntimeException_init_$Init$_0(message, $this) {
  Exception_init_$Init$_0(message, $this);
  RuntimeException.call($this);
  return $this;
}
function RuntimeException_init_$Create$_0(message) {
  var tmp = RuntimeException_init_$Init$_0(message, objectCreate(protoOf(RuntimeException)));
  captureStack(tmp, RuntimeException_init_$Create$_0);
  return tmp;
}
function RuntimeException_init_$Init$_1(message, cause, $this) {
  Exception_init_$Init$_1(message, cause, $this);
  RuntimeException.call($this);
  return $this;
}
function RuntimeException() {
  captureStack(this, RuntimeException);
}
function Exception_init_$Init$($this) {
  extendThrowable($this);
  Exception.call($this);
  return $this;
}
function Exception_init_$Create$() {
  var tmp = Exception_init_$Init$(objectCreate(protoOf(Exception)));
  captureStack(tmp, Exception_init_$Create$);
  return tmp;
}
function Exception_init_$Init$_0(message, $this) {
  extendThrowable($this, message);
  Exception.call($this);
  return $this;
}
function Exception_init_$Create$_0(message) {
  var tmp = Exception_init_$Init$_0(message, objectCreate(protoOf(Exception)));
  captureStack(tmp, Exception_init_$Create$_0);
  return tmp;
}
function Exception_init_$Init$_1(message, cause, $this) {
  extendThrowable($this, message, cause);
  Exception.call($this);
  return $this;
}
function Exception() {
  captureStack(this, Exception);
}
function NoSuchElementException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  NoSuchElementException.call($this);
  return $this;
}
function NoSuchElementException_init_$Create$() {
  var tmp = NoSuchElementException_init_$Init$(objectCreate(protoOf(NoSuchElementException)));
  captureStack(tmp, NoSuchElementException_init_$Create$);
  return tmp;
}
function NoSuchElementException_init_$Init$_0(message, $this) {
  RuntimeException_init_$Init$_0(message, $this);
  NoSuchElementException.call($this);
  return $this;
}
function NoSuchElementException_init_$Create$_0(message) {
  var tmp = NoSuchElementException_init_$Init$_0(message, objectCreate(protoOf(NoSuchElementException)));
  captureStack(tmp, NoSuchElementException_init_$Create$_0);
  return tmp;
}
function NoSuchElementException() {
  captureStack(this, NoSuchElementException);
}
function IndexOutOfBoundsException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  IndexOutOfBoundsException.call($this);
  return $this;
}
function IndexOutOfBoundsException_init_$Create$() {
  var tmp = IndexOutOfBoundsException_init_$Init$(objectCreate(protoOf(IndexOutOfBoundsException)));
  captureStack(tmp, IndexOutOfBoundsException_init_$Create$);
  return tmp;
}
function IndexOutOfBoundsException_init_$Init$_0(message, $this) {
  RuntimeException_init_$Init$_0(message, $this);
  IndexOutOfBoundsException.call($this);
  return $this;
}
function IndexOutOfBoundsException_init_$Create$_0(message) {
  var tmp = IndexOutOfBoundsException_init_$Init$_0(message, objectCreate(protoOf(IndexOutOfBoundsException)));
  captureStack(tmp, IndexOutOfBoundsException_init_$Create$_0);
  return tmp;
}
function IndexOutOfBoundsException() {
  captureStack(this, IndexOutOfBoundsException);
}
function IllegalStateException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  IllegalStateException.call($this);
  return $this;
}
function IllegalStateException_init_$Create$() {
  var tmp = IllegalStateException_init_$Init$(objectCreate(protoOf(IllegalStateException)));
  captureStack(tmp, IllegalStateException_init_$Create$);
  return tmp;
}
function IllegalStateException_init_$Init$_0(message, $this) {
  RuntimeException_init_$Init$_0(message, $this);
  IllegalStateException.call($this);
  return $this;
}
function IllegalStateException_init_$Create$_0(message) {
  var tmp = IllegalStateException_init_$Init$_0(message, objectCreate(protoOf(IllegalStateException)));
  captureStack(tmp, IllegalStateException_init_$Create$_0);
  return tmp;
}
function IllegalStateException() {
  captureStack(this, IllegalStateException);
}
function ConcurrentModificationException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  ConcurrentModificationException.call($this);
  return $this;
}
function ConcurrentModificationException_init_$Create$() {
  var tmp = ConcurrentModificationException_init_$Init$(objectCreate(protoOf(ConcurrentModificationException)));
  captureStack(tmp, ConcurrentModificationException_init_$Create$);
  return tmp;
}
function ConcurrentModificationException_init_$Init$_0(message, $this) {
  RuntimeException_init_$Init$_0(message, $this);
  ConcurrentModificationException.call($this);
  return $this;
}
function ConcurrentModificationException_init_$Create$_0(message) {
  var tmp = ConcurrentModificationException_init_$Init$_0(message, objectCreate(protoOf(ConcurrentModificationException)));
  captureStack(tmp, ConcurrentModificationException_init_$Create$_0);
  return tmp;
}
function ConcurrentModificationException() {
  captureStack(this, ConcurrentModificationException);
}
function ArithmeticException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  ArithmeticException.call($this);
  return $this;
}
function ArithmeticException_init_$Create$() {
  var tmp = ArithmeticException_init_$Init$(objectCreate(protoOf(ArithmeticException)));
  captureStack(tmp, ArithmeticException_init_$Create$);
  return tmp;
}
function ArithmeticException_init_$Init$_0(message, $this) {
  RuntimeException_init_$Init$_0(message, $this);
  ArithmeticException.call($this);
  return $this;
}
function ArithmeticException_init_$Create$_0(message) {
  var tmp = ArithmeticException_init_$Init$_0(message, objectCreate(protoOf(ArithmeticException)));
  captureStack(tmp, ArithmeticException_init_$Create$_0);
  return tmp;
}
function ArithmeticException() {
  captureStack(this, ArithmeticException);
}
function NumberFormatException_init_$Init$($this) {
  IllegalArgumentException_init_$Init$($this);
  NumberFormatException.call($this);
  return $this;
}
function NumberFormatException_init_$Create$() {
  var tmp = NumberFormatException_init_$Init$(objectCreate(protoOf(NumberFormatException)));
  captureStack(tmp, NumberFormatException_init_$Create$);
  return tmp;
}
function NumberFormatException_init_$Init$_0(message, $this) {
  IllegalArgumentException_init_$Init$_0(message, $this);
  NumberFormatException.call($this);
  return $this;
}
function NumberFormatException_init_$Create$_0(message) {
  var tmp = NumberFormatException_init_$Init$_0(message, objectCreate(protoOf(NumberFormatException)));
  captureStack(tmp, NumberFormatException_init_$Create$_0);
  return tmp;
}
function NumberFormatException() {
  captureStack(this, NumberFormatException);
}
function NoWhenBranchMatchedException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  NoWhenBranchMatchedException.call($this);
  return $this;
}
function NoWhenBranchMatchedException_init_$Create$() {
  var tmp = NoWhenBranchMatchedException_init_$Init$(objectCreate(protoOf(NoWhenBranchMatchedException)));
  captureStack(tmp, NoWhenBranchMatchedException_init_$Create$);
  return tmp;
}
function NoWhenBranchMatchedException() {
  captureStack(this, NoWhenBranchMatchedException);
}
function NullPointerException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  NullPointerException.call($this);
  return $this;
}
function NullPointerException_init_$Create$() {
  var tmp = NullPointerException_init_$Init$(objectCreate(protoOf(NullPointerException)));
  captureStack(tmp, NullPointerException_init_$Create$);
  return tmp;
}
function NullPointerException() {
  captureStack(this, NullPointerException);
}
function ClassCastException_init_$Init$($this) {
  RuntimeException_init_$Init$($this);
  ClassCastException.call($this);
  return $this;
}
function ClassCastException_init_$Create$() {
  var tmp = ClassCastException_init_$Init$(objectCreate(protoOf(ClassCastException)));
  captureStack(tmp, ClassCastException_init_$Create$);
  return tmp;
}
function ClassCastException() {
  captureStack(this, ClassCastException);
}
function fillFrom(src, dst) {
  var srcLen = src.length;
  var dstLen = dst.length;
  var index = 0;
  // Inline function 'kotlin.js.unsafeCast' call
  var arr = dst;
  while (index < srcLen && index < dstLen) {
    var tmp = index;
    var _unary__edvuaz = index;
    index = _unary__edvuaz + 1 | 0;
    arr[tmp] = src[_unary__edvuaz];
  }
  return dst;
}
function arrayCopyResize(source, newSize, defaultValue) {
  // Inline function 'kotlin.js.unsafeCast' call
  var result = source.slice(0, newSize);
  // Inline function 'kotlin.copyArrayType' call
  if (source.$type$ !== undefined) {
    result.$type$ = source.$type$;
  }
  var index = source.length;
  if (newSize > index) {
    // Inline function 'kotlin.js.asDynamic' call
    result.length = newSize;
    while (index < newSize) {
      var _unary__edvuaz = index;
      index = _unary__edvuaz + 1 | 0;
      result[_unary__edvuaz] = defaultValue;
    }
  }
  return result;
}
function lazy(initializer) {
  return new UnsafeLazyImpl(initializer);
}
function PrimitiveKClassImpl(jClass, simpleName, isInstanceFunction) {
  KClassImpl.call(this);
  this.h6_1 = jClass;
  this.i6_1 = simpleName;
  this.j6_1 = isInstanceFunction;
}
protoOf(PrimitiveKClassImpl).k6 = function () {
  return this.h6_1;
};
protoOf(PrimitiveKClassImpl).l6 = function () {
  return this.i6_1;
};
function KClassImpl() {
}
protoOf(KClassImpl).equals = function (other) {
  var tmp;
  if (other instanceof NothingKClassImpl) {
    tmp = false;
  } else {
    if (other instanceof KClassImpl) {
      tmp = (equals(this.k6(), other.k6()) && this.l6() == other.l6());
    } else {
      tmp = false;
    }
  }
  return tmp;
};
protoOf(KClassImpl).hashCode = function () {
  var tmp0_safe_receiver = this.l6();
  var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : getStringHashCode(tmp0_safe_receiver);
  return tmp1_elvis_lhs == null ? 0 : tmp1_elvis_lhs;
};
protoOf(KClassImpl).toString = function () {
  return 'class ' + this.l6();
};
function NothingKClassImpl() {
}
function KProperty1() {
}
function reset(_this__u8e3s4) {
  _this__u8e3s4.lastIndex = 0;
}
function StringBuilder_init_$Init$(capacity, $this) {
  StringBuilder_init_$Init$_0($this);
  return $this;
}
function StringBuilder_init_$Create$(capacity) {
  return StringBuilder_init_$Init$(capacity, objectCreate(protoOf(StringBuilder)));
}
function StringBuilder_init_$Init$_0($this) {
  StringBuilder.call($this, '');
  return $this;
}
function StringBuilder_init_$Create$_0() {
  return StringBuilder_init_$Init$_0(objectCreate(protoOf(StringBuilder)));
}
function StringBuilder(content) {
  this.q5_1 = content;
}
protoOf(StringBuilder).a = function () {
  // Inline function 'kotlin.js.asDynamic' call
  return this.q5_1.length;
};
protoOf(StringBuilder).b = function (index) {
  // Inline function 'kotlin.text.getOrElse' call
  var this_0 = this.q5_1;
  var tmp;
  if (0 <= index ? index <= (charSequenceLength(this_0) - 1 | 0) : false) {
    tmp = charSequenceGet(this_0, index);
  } else {
    throw IndexOutOfBoundsException_init_$Create$_0('index: ' + index + ', length: ' + this.a() + '}');
  }
  return tmp;
};
protoOf(StringBuilder).c = function (startIndex, endIndex) {
  return substring(this.q5_1, startIndex, endIndex);
};
protoOf(StringBuilder).t5 = function (value) {
  this.q5_1 = this.q5_1 + toString(value);
  return this;
};
protoOf(StringBuilder).e = function (value) {
  this.q5_1 = this.q5_1 + toString_0(value);
  return this;
};
protoOf(StringBuilder).m6 = function (value, startIndex, endIndex) {
  return this.n6(value == null ? 'null' : value, startIndex, endIndex);
};
protoOf(StringBuilder).r5 = function (value) {
  this.q5_1 = this.q5_1 + toString_0(value);
  return this;
};
protoOf(StringBuilder).s5 = function (value) {
  var tmp = this;
  var tmp_0 = this.q5_1;
  tmp.q5_1 = tmp_0 + (value == null ? 'null' : value);
  return this;
};
protoOf(StringBuilder).toString = function () {
  return this.q5_1;
};
protoOf(StringBuilder).n6 = function (value, startIndex, endIndex) {
  var stringCsq = toString_1(value);
  Companion_instance_5.o6(startIndex, endIndex, stringCsq.length);
  this.q5_1 = this.q5_1 + substring(stringCsq, startIndex, endIndex);
  return this;
};
function uppercaseChar(_this__u8e3s4) {
  // Inline function 'kotlin.text.uppercase' call
  // Inline function 'kotlin.js.asDynamic' call
  // Inline function 'kotlin.js.unsafeCast' call
  var uppercase = toString(_this__u8e3s4).toUpperCase();
  return uppercase.length > 1 ? _this__u8e3s4 : charCodeAt(uppercase, 0);
}
function isWhitespace(_this__u8e3s4) {
  return isWhitespaceImpl(_this__u8e3s4);
}
function isDigit(_this__u8e3s4) {
  if (_Char___init__impl__6a9atx(48) <= _this__u8e3s4 ? _this__u8e3s4 <= _Char___init__impl__6a9atx(57) : false) {
    return true;
  }
  if (Char__compareTo_impl_ypi4mb(_this__u8e3s4, _Char___init__impl__6a9atx(128)) < 0) {
    return false;
  }
  return isDigitImpl(_this__u8e3s4);
}
function isLetter(_this__u8e3s4) {
  if ((_Char___init__impl__6a9atx(97) <= _this__u8e3s4 ? _this__u8e3s4 <= _Char___init__impl__6a9atx(122) : false) || (_Char___init__impl__6a9atx(65) <= _this__u8e3s4 ? _this__u8e3s4 <= _Char___init__impl__6a9atx(90) : false)) {
    return true;
  }
  if (Char__compareTo_impl_ypi4mb(_this__u8e3s4, _Char___init__impl__6a9atx(128)) < 0) {
    return false;
  }
  return isLetterImpl(_this__u8e3s4);
}
function checkRadix(radix) {
  if (!(2 <= radix ? radix <= 36 : false)) {
    throw IllegalArgumentException_init_$Create$_0('radix ' + radix + ' was not in valid range 2..36');
  }
  return radix;
}
function digitOf(char, radix) {
  // Inline function 'kotlin.let' call
  var it = Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(48)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(57)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(48)) : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(90)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(65)) + 10 | 0 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(97)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(122)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(97)) + 10 | 0 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(128)) < 0 ? -1 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65313)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65338)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(65313)) + 10 | 0 : Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65345)) >= 0 && Char__compareTo_impl_ypi4mb(char, _Char___init__impl__6a9atx(65370)) <= 0 ? Char__minus_impl_a2frrh(char, _Char___init__impl__6a9atx(65345)) + 10 | 0 : digitToIntImpl(char);
  return it >= radix ? -1 : it;
}
function toInt(_this__u8e3s4) {
  var tmp0_elvis_lhs = toIntOrNull(_this__u8e3s4);
  var tmp;
  if (tmp0_elvis_lhs == null) {
    numberFormatError(_this__u8e3s4);
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
}
function Regex_init_$Init$(pattern, $this) {
  Regex.call($this, pattern, emptySet());
  return $this;
}
function Regex_init_$Create$(pattern) {
  return Regex_init_$Init$(pattern, objectCreate(protoOf(Regex)));
}
function initMatchesEntirePattern($this) {
  var tmp0_elvis_lhs = $this.t6_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.run' call
    var tmp_0;
    if (startsWith_0($this.p6_1, _Char___init__impl__6a9atx(94)) && endsWith($this.p6_1, _Char___init__impl__6a9atx(36))) {
      tmp_0 = $this.r6_1;
    } else {
      return new RegExp('^' + trimEnd(trimStart($this.p6_1, charArrayOf([_Char___init__impl__6a9atx(94)])), charArrayOf([_Char___init__impl__6a9atx(36)])) + '$', toFlags($this.q6_1, 'gu'));
    }
    // Inline function 'kotlin.also' call
    var this_0 = tmp_0;
    $this.t6_1 = this_0;
    tmp = this_0;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
}
function Companion_4() {
  Companion_instance_4 = this;
  this.u6_1 = new RegExp('[\\\\^$*+?.()|[\\]{}]', 'g');
  this.v6_1 = new RegExp('[\\\\$]', 'g');
  this.w6_1 = new RegExp('\\$', 'g');
}
var Companion_instance_4;
function Companion_getInstance_4() {
  if (Companion_instance_4 == null)
    new Companion_4();
  return Companion_instance_4;
}
function Regex$findAll$lambda(this$0, $input, $startIndex) {
  return function () {
    return this$0.x6($input, $startIndex);
  };
}
function Regex$findAll$lambda_0(match) {
  return match.i();
}
function Regex$replace$lambda($replacement) {
  return function (it) {
    return substituteGroupRefs(it, $replacement);
  };
}
function Regex(pattern, options) {
  Companion_getInstance_4();
  this.p6_1 = pattern;
  this.q6_1 = toSet(options);
  this.r6_1 = new RegExp(pattern, toFlags(options, 'gu'));
  this.s6_1 = null;
  this.t6_1 = null;
}
protoOf(Regex).y6 = function (input) {
  reset(this.r6_1);
  var match = this.r6_1.exec(toString_1(input));
  return !(match == null) && match.index === 0 && this.r6_1.lastIndex === charSequenceLength(input);
};
protoOf(Regex).x6 = function (input, startIndex) {
  if (startIndex < 0 || startIndex > charSequenceLength(input)) {
    throw IndexOutOfBoundsException_init_$Create$_0('Start index out of bounds: ' + startIndex + ', input length: ' + charSequenceLength(input));
  }
  return findNext(this.r6_1, toString_1(input), startIndex, this.r6_1);
};
protoOf(Regex).z6 = function (input, startIndex, $super) {
  startIndex = startIndex === VOID ? 0 : startIndex;
  return $super === VOID ? this.x6(input, startIndex) : $super.x6.call(this, input, startIndex);
};
protoOf(Regex).a7 = function (input, startIndex) {
  if (startIndex < 0 || startIndex > charSequenceLength(input)) {
    throw IndexOutOfBoundsException_init_$Create$_0('Start index out of bounds: ' + startIndex + ', input length: ' + charSequenceLength(input));
  }
  var tmp = Regex$findAll$lambda(this, input, startIndex);
  return generateSequence(tmp, Regex$findAll$lambda_0);
};
protoOf(Regex).b7 = function (input, startIndex, $super) {
  startIndex = startIndex === VOID ? 0 : startIndex;
  return $super === VOID ? this.a7(input, startIndex) : $super.a7.call(this, input, startIndex);
};
protoOf(Regex).c7 = function (input) {
  return findNext(initMatchesEntirePattern(this), toString_1(input), 0, this.r6_1);
};
protoOf(Regex).d7 = function (input, replacement) {
  if (!contains_1(replacement, _Char___init__impl__6a9atx(92)) && !contains_1(replacement, _Char___init__impl__6a9atx(36))) {
    var tmp0 = toString_1(input);
    // Inline function 'kotlin.text.nativeReplace' call
    var pattern = this.r6_1;
    // Inline function 'kotlin.js.asDynamic' call
    return tmp0.replace(pattern, replacement);
  }
  return this.e7(input, Regex$replace$lambda(replacement));
};
protoOf(Regex).e7 = function (input, transform) {
  var match = this.z6(input);
  if (match == null)
    return toString_1(input);
  var lastStart = 0;
  var length = charSequenceLength(input);
  var sb = StringBuilder_init_$Create$(length);
  do {
    var foundMatch = ensureNotNull(match);
    sb.m6(input, lastStart, foundMatch.f7().j7());
    sb.e(transform(foundMatch));
    lastStart = foundMatch.f7().k7() + 1 | 0;
    match = foundMatch.i();
  }
   while (lastStart < length && !(match == null));
  if (lastStart < length) {
    sb.m6(input, lastStart, length);
  }
  return sb.toString();
};
protoOf(Regex).l7 = function (input, limit) {
  requireNonNegativeLimit(limit);
  // Inline function 'kotlin.let' call
  var it = this.b7(input);
  var matches = limit === 0 ? it : take_0(it, limit - 1 | 0);
  // Inline function 'kotlin.collections.mutableListOf' call
  var result = ArrayList_init_$Create$();
  var lastStart = 0;
  var _iterator__ex2g4s = matches.g();
  while (_iterator__ex2g4s.h()) {
    var match = _iterator__ex2g4s.i();
    result.f(toString_1(charSequenceSubSequence(input, lastStart, match.f7().j7())));
    lastStart = match.f7().k7() + 1 | 0;
  }
  result.f(toString_1(charSequenceSubSequence(input, lastStart, charSequenceLength(input))));
  return result;
};
protoOf(Regex).toString = function () {
  return this.r6_1.toString();
};
function toFlags(_this__u8e3s4, prepend) {
  return joinToString_0(_this__u8e3s4, '', prepend, VOID, VOID, VOID, toFlags$lambda);
}
function findNext(_this__u8e3s4, input, from, nextPattern) {
  _this__u8e3s4.lastIndex = from;
  var match = _this__u8e3s4.exec(input);
  if (match == null)
    return null;
  var range = numberRangeToNumber(match.index, _this__u8e3s4.lastIndex - 1 | 0);
  return new findNext$1(range, match, nextPattern, input);
}
function substituteGroupRefs(match, replacement) {
  var index = 0;
  var result = StringBuilder_init_$Create$_0();
  while (index < replacement.length) {
    var _unary__edvuaz = index;
    index = _unary__edvuaz + 1 | 0;
    var char = charCodeAt(replacement, _unary__edvuaz);
    if (char === _Char___init__impl__6a9atx(92)) {
      if (index === replacement.length)
        throw IllegalArgumentException_init_$Create$_0('The Char to be escaped is missing');
      var _unary__edvuaz_0 = index;
      index = _unary__edvuaz_0 + 1 | 0;
      result.t5(charCodeAt(replacement, _unary__edvuaz_0));
    } else if (char === _Char___init__impl__6a9atx(36)) {
      if (index === replacement.length)
        throw IllegalArgumentException_init_$Create$_0('Capturing group index is missing');
      if (charCodeAt(replacement, index) === _Char___init__impl__6a9atx(123)) {
        index = index + 1 | 0;
        var endIndex = readGroupName(replacement, index);
        if (index === endIndex)
          throw IllegalArgumentException_init_$Create$_0('Named capturing group reference should have a non-empty name');
        if (endIndex === replacement.length || !(charCodeAt(replacement, endIndex) === _Char___init__impl__6a9atx(125)))
          throw IllegalArgumentException_init_$Create$_0("Named capturing group reference is missing trailing '}'");
        var groupName = substring(replacement, index, endIndex);
        var tmp0_safe_receiver = get(match.m7(), groupName);
        var tmp1_elvis_lhs = tmp0_safe_receiver == null ? null : tmp0_safe_receiver.n7_1;
        result.s5(tmp1_elvis_lhs == null ? '' : tmp1_elvis_lhs);
        index = endIndex + 1 | 0;
      } else {
        var containsArg = charCodeAt(replacement, index);
        if (!(_Char___init__impl__6a9atx(48) <= containsArg ? containsArg <= _Char___init__impl__6a9atx(57) : false))
          throw IllegalArgumentException_init_$Create$_0('Invalid capturing group reference');
        var groups = match.m7();
        var endIndex_0 = readGroupIndex(replacement, index, groups.j());
        var groupIndex = toInt(substring(replacement, index, endIndex_0));
        if (groupIndex >= groups.j())
          throw IndexOutOfBoundsException_init_$Create$_0('Group with index ' + groupIndex + ' does not exist');
        var tmp2_safe_receiver = groups.k(groupIndex);
        var tmp3_elvis_lhs = tmp2_safe_receiver == null ? null : tmp2_safe_receiver.n7_1;
        result.s5(tmp3_elvis_lhs == null ? '' : tmp3_elvis_lhs);
        index = endIndex_0;
      }
    } else {
      result.t5(char);
    }
  }
  return result.toString();
}
function MatchGroup(value) {
  this.n7_1 = value;
}
protoOf(MatchGroup).toString = function () {
  return 'MatchGroup(value=' + this.n7_1 + ')';
};
protoOf(MatchGroup).hashCode = function () {
  return getStringHashCode(this.n7_1);
};
protoOf(MatchGroup).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof MatchGroup))
    return false;
  if (!(this.n7_1 === other.n7_1))
    return false;
  return true;
};
function readGroupName(_this__u8e3s4, startIndex) {
  var index = startIndex;
  $l$loop: while (index < _this__u8e3s4.length) {
    if (charCodeAt(_this__u8e3s4, index) === _Char___init__impl__6a9atx(125)) {
      break $l$loop;
    } else {
      index = index + 1 | 0;
    }
  }
  return index;
}
function get(_this__u8e3s4, name) {
  var tmp0_elvis_lhs = isInterface(_this__u8e3s4, MatchNamedGroupCollection) ? _this__u8e3s4 : null;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    throw UnsupportedOperationException_init_$Create$_0('Retrieving groups by name is not supported on this platform.');
  } else {
    tmp = tmp0_elvis_lhs;
  }
  var namedGroups = tmp;
  return namedGroups.o7(name);
}
function readGroupIndex(_this__u8e3s4, startIndex, groupCount) {
  var index = startIndex + 1 | 0;
  var groupIndex = Char__minus_impl_a2frrh(charCodeAt(_this__u8e3s4, startIndex), _Char___init__impl__6a9atx(48));
  $l$loop_0: while (true) {
    var tmp;
    if (index < _this__u8e3s4.length) {
      var containsArg = charCodeAt(_this__u8e3s4, index);
      tmp = _Char___init__impl__6a9atx(48) <= containsArg ? containsArg <= _Char___init__impl__6a9atx(57) : false;
    } else {
      tmp = false;
    }
    if (!tmp) {
      break $l$loop_0;
    }
    var newGroupIndex = imul_0(groupIndex, 10) + Char__minus_impl_a2frrh(charCodeAt(_this__u8e3s4, index), _Char___init__impl__6a9atx(48)) | 0;
    if (0 <= newGroupIndex ? newGroupIndex < groupCount : false) {
      groupIndex = newGroupIndex;
      index = index + 1 | 0;
    } else {
      break $l$loop_0;
    }
  }
  return index;
}
function toFlags$lambda(it) {
  return it.r7_1;
}
function findNext$o$groups$o$iterator$lambda(this$0) {
  return function (it) {
    return this$0.k(it);
  };
}
function hasOwnPrototypeProperty($this, o, name) {
  // Inline function 'kotlin.js.unsafeCast' call
  return Object.prototype.hasOwnProperty.call(o, name);
}
function advanceToNextCharacter($this, index) {
  if (index < get_lastIndex_1($this.a8_1)) {
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    var code1 = $this.a8_1.charCodeAt(index);
    if (55296 <= code1 ? code1 <= 56319 : false) {
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      var code2 = $this.a8_1.charCodeAt(index + 1 | 0);
      if (56320 <= code2 ? code2 <= 57343 : false) {
        return index + 2 | 0;
      }
    }
  }
  return index + 1 | 0;
}
function findNext$1$groups$1($match, this$0) {
  this.s7_1 = $match;
  this.t7_1 = this$0;
  AbstractCollection.call(this);
}
protoOf(findNext$1$groups$1).j = function () {
  return this.s7_1.length;
};
protoOf(findNext$1$groups$1).g = function () {
  var tmp = asSequence(get_indices(this));
  return map(tmp, findNext$o$groups$o$iterator$lambda(this)).g();
};
protoOf(findNext$1$groups$1).k = function (index) {
  // Inline function 'kotlin.js.get' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp0_safe_receiver = this.s7_1[index];
  var tmp;
  if (tmp0_safe_receiver == null) {
    tmp = null;
  } else {
    // Inline function 'kotlin.let' call
    tmp = new MatchGroup(tmp0_safe_receiver);
  }
  return tmp;
};
protoOf(findNext$1$groups$1).o7 = function (name) {
  // Inline function 'kotlin.js.asDynamic' call
  var tmp0_elvis_lhs = this.s7_1.groups;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    throw IllegalArgumentException_init_$Create$_0('Capturing group with name {' + name + '} does not exist. No named capturing group was defined in Regex');
  } else {
    tmp = tmp0_elvis_lhs;
  }
  var groups = tmp;
  if (!hasOwnPrototypeProperty(this.t7_1, groups, name))
    throw IllegalArgumentException_init_$Create$_0('Capturing group with name {' + name + '} does not exist');
  var value = groups[name];
  var tmp_0;
  if (value == undefined) {
    tmp_0 = null;
  } else {
    tmp_0 = new MatchGroup((!(value == null) ? typeof value === 'string' : false) ? value : THROW_CCE());
  }
  return tmp_0;
};
function findNext$1$groupValues$1($match) {
  this.b8_1 = $match;
  AbstractList.call(this);
}
protoOf(findNext$1$groupValues$1).j = function () {
  return this.b8_1.length;
};
protoOf(findNext$1$groupValues$1).k = function (index) {
  // Inline function 'kotlin.js.get' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp0_elvis_lhs = this.b8_1[index];
  return tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs;
};
function findNext$1($range, $match, $nextPattern, $input) {
  this.x7_1 = $range;
  this.y7_1 = $match;
  this.z7_1 = $nextPattern;
  this.a8_1 = $input;
  this.u7_1 = $range;
  var tmp = this;
  tmp.v7_1 = new findNext$1$groups$1($match, this);
  this.w7_1 = null;
}
protoOf(findNext$1).f7 = function () {
  return this.u7_1;
};
protoOf(findNext$1).m7 = function () {
  return this.v7_1;
};
protoOf(findNext$1).c8 = function () {
  if (this.w7_1 == null) {
    var tmp = this;
    tmp.w7_1 = new findNext$1$groupValues$1(this.y7_1);
  }
  return ensureNotNull(this.w7_1);
};
protoOf(findNext$1).i = function () {
  return findNext(this.z7_1, this.a8_1, this.x7_1.l() ? advanceToNextCharacter(this, this.x7_1.j7()) : this.x7_1.k7() + 1 | 0, this.z7_1);
};
var STRING_CASE_INSENSITIVE_ORDER;
function substring(_this__u8e3s4, startIndex, endIndex) {
  _init_properties_stringJs_kt__bg7zye();
  // Inline function 'kotlin.js.asDynamic' call
  return _this__u8e3s4.substring(startIndex, endIndex);
}
function substring_0(_this__u8e3s4, startIndex) {
  _init_properties_stringJs_kt__bg7zye();
  // Inline function 'kotlin.js.asDynamic' call
  return _this__u8e3s4.substring(startIndex);
}
function compareTo_0(_this__u8e3s4, other, ignoreCase) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  _init_properties_stringJs_kt__bg7zye();
  if (ignoreCase) {
    var n1 = _this__u8e3s4.length;
    var n2 = other.length;
    // Inline function 'kotlin.comparisons.minOf' call
    var min = Math.min(n1, n2);
    if (min === 0)
      return n1 - n2 | 0;
    var inductionVariable = 0;
    if (inductionVariable < min)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var thisChar = charCodeAt(_this__u8e3s4, index);
        var otherChar = charCodeAt(other, index);
        if (!(thisChar === otherChar)) {
          thisChar = uppercaseChar(thisChar);
          otherChar = uppercaseChar(otherChar);
          if (!(thisChar === otherChar)) {
            // Inline function 'kotlin.text.lowercaseChar' call
            // Inline function 'kotlin.text.lowercase' call
            var this_0 = thisChar;
            // Inline function 'kotlin.js.asDynamic' call
            // Inline function 'kotlin.js.unsafeCast' call
            var tmp$ret$2 = toString(this_0).toLowerCase();
            thisChar = charCodeAt(tmp$ret$2, 0);
            // Inline function 'kotlin.text.lowercaseChar' call
            // Inline function 'kotlin.text.lowercase' call
            var this_1 = otherChar;
            // Inline function 'kotlin.js.asDynamic' call
            // Inline function 'kotlin.js.unsafeCast' call
            var tmp$ret$6 = toString(this_1).toLowerCase();
            otherChar = charCodeAt(tmp$ret$6, 0);
            if (!(thisChar === otherChar)) {
              return Char__compareTo_impl_ypi4mb(thisChar, otherChar);
            }
          }
        }
      }
       while (inductionVariable < min);
    return n1 - n2 | 0;
  } else {
    return compareTo(_this__u8e3s4, other);
  }
}
function sam$kotlin_Comparator$0(function_0) {
  this.d8_1 = function_0;
}
protoOf(sam$kotlin_Comparator$0).e8 = function (a, b) {
  return this.d8_1(a, b);
};
protoOf(sam$kotlin_Comparator$0).compare = function (a, b) {
  return this.e8(a, b);
};
protoOf(sam$kotlin_Comparator$0).a2 = function () {
  return this.d8_1;
};
protoOf(sam$kotlin_Comparator$0).equals = function (other) {
  var tmp;
  if (!(other == null) ? isInterface(other, Comparator) : false) {
    var tmp_0;
    if (!(other == null) ? isInterface(other, FunctionAdapter) : false) {
      tmp_0 = equals(this.a2(), other.a2());
    } else {
      tmp_0 = false;
    }
    tmp = tmp_0;
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(sam$kotlin_Comparator$0).hashCode = function () {
  return hashCode_0(this.a2());
};
function STRING_CASE_INSENSITIVE_ORDER$lambda(a, b) {
  _init_properties_stringJs_kt__bg7zye();
  return compareTo_0(a, b, true);
}
var properties_initialized_stringJs_kt_nta8o4;
function _init_properties_stringJs_kt__bg7zye() {
  if (!properties_initialized_stringJs_kt_nta8o4) {
    properties_initialized_stringJs_kt_nta8o4 = true;
    var tmp = STRING_CASE_INSENSITIVE_ORDER$lambda;
    STRING_CASE_INSENSITIVE_ORDER = new sam$kotlin_Comparator$0(tmp);
  }
}
function startsWith(_this__u8e3s4, prefix, ignoreCase) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  if (!ignoreCase) {
    // Inline function 'kotlin.text.nativeStartsWith' call
    // Inline function 'kotlin.js.asDynamic' call
    return _this__u8e3s4.startsWith(prefix, 0);
  } else
    return regionMatches(_this__u8e3s4, 0, prefix, 0, prefix.length, ignoreCase);
}
function regionMatches(_this__u8e3s4, thisOffset, other, otherOffset, length, ignoreCase) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  return regionMatchesImpl(_this__u8e3s4, thisOffset, other, otherOffset, length, ignoreCase);
}
function AbstractCollection$toString$lambda(this$0) {
  return function (it) {
    return it === this$0 ? '(this Collection)' : toString_0(it);
  };
}
function AbstractCollection() {
}
protoOf(AbstractCollection).h1 = function (element) {
  var tmp$ret$0;
  $l$block_0: {
    // Inline function 'kotlin.collections.any' call
    var tmp;
    if (isInterface(this, Collection)) {
      tmp = this.l();
    } else {
      tmp = false;
    }
    if (tmp) {
      tmp$ret$0 = false;
      break $l$block_0;
    }
    var _iterator__ex2g4s = this.g();
    while (_iterator__ex2g4s.h()) {
      var element_0 = _iterator__ex2g4s.i();
      if (equals(element_0, element)) {
        tmp$ret$0 = true;
        break $l$block_0;
      }
    }
    tmp$ret$0 = false;
  }
  return tmp$ret$0;
};
protoOf(AbstractCollection).i1 = function (elements) {
  var tmp$ret$0;
  $l$block_0: {
    // Inline function 'kotlin.collections.all' call
    var tmp;
    if (isInterface(elements, Collection)) {
      tmp = elements.l();
    } else {
      tmp = false;
    }
    if (tmp) {
      tmp$ret$0 = true;
      break $l$block_0;
    }
    var _iterator__ex2g4s = elements.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if (!this.h1(element)) {
        tmp$ret$0 = false;
        break $l$block_0;
      }
    }
    tmp$ret$0 = true;
  }
  return tmp$ret$0;
};
protoOf(AbstractCollection).l = function () {
  return this.j() === 0;
};
protoOf(AbstractCollection).toString = function () {
  return joinToString_0(this, ', ', '[', ']', VOID, VOID, AbstractCollection$toString$lambda(this));
};
protoOf(AbstractCollection).toArray = function () {
  return collectionToArray(this);
};
function IteratorImpl_0($outer) {
  this.g8_1 = $outer;
  this.f8_1 = 0;
}
protoOf(IteratorImpl_0).h = function () {
  return this.f8_1 < this.g8_1.j();
};
protoOf(IteratorImpl_0).i = function () {
  if (!this.h())
    throw NoSuchElementException_init_$Create$();
  var _unary__edvuaz = this.f8_1;
  this.f8_1 = _unary__edvuaz + 1 | 0;
  return this.g8_1.k(_unary__edvuaz);
};
function Companion_5() {
  this.f2_1 = 2147483639;
}
protoOf(Companion_5).d3 = function (index, size) {
  if (index < 0 || index >= size) {
    throw IndexOutOfBoundsException_init_$Create$_0('index: ' + index + ', size: ' + size);
  }
};
protoOf(Companion_5).e3 = function (index, size) {
  if (index < 0 || index > size) {
    throw IndexOutOfBoundsException_init_$Create$_0('index: ' + index + ', size: ' + size);
  }
};
protoOf(Companion_5).g2 = function (fromIndex, toIndex, size) {
  if (fromIndex < 0 || toIndex > size) {
    throw IndexOutOfBoundsException_init_$Create$_0('fromIndex: ' + fromIndex + ', toIndex: ' + toIndex + ', size: ' + size);
  }
  if (fromIndex > toIndex) {
    throw IllegalArgumentException_init_$Create$_0('fromIndex: ' + fromIndex + ' > toIndex: ' + toIndex);
  }
};
protoOf(Companion_5).o6 = function (startIndex, endIndex, size) {
  if (startIndex < 0 || endIndex > size) {
    throw IndexOutOfBoundsException_init_$Create$_0('startIndex: ' + startIndex + ', endIndex: ' + endIndex + ', size: ' + size);
  }
  if (startIndex > endIndex) {
    throw IllegalArgumentException_init_$Create$_0('startIndex: ' + startIndex + ' > endIndex: ' + endIndex);
  }
};
protoOf(Companion_5).r4 = function (oldCapacity, minCapacity) {
  var newCapacity = oldCapacity + (oldCapacity >> 1) | 0;
  if ((newCapacity - minCapacity | 0) < 0)
    newCapacity = minCapacity;
  if ((newCapacity - 2147483639 | 0) > 0)
    newCapacity = minCapacity > 2147483639 ? 2147483647 : 2147483639;
  return newCapacity;
};
protoOf(Companion_5).q2 = function (c) {
  var hashCode = 1;
  var _iterator__ex2g4s = c.g();
  while (_iterator__ex2g4s.h()) {
    var e = _iterator__ex2g4s.i();
    var tmp = imul_0(31, hashCode);
    var tmp1_elvis_lhs = e == null ? null : hashCode_0(e);
    hashCode = tmp + (tmp1_elvis_lhs == null ? 0 : tmp1_elvis_lhs) | 0;
  }
  return hashCode;
};
protoOf(Companion_5).p2 = function (c, other) {
  if (!(c.j() === other.j()))
    return false;
  var otherIterator = other.g();
  var _iterator__ex2g4s = c.g();
  while (_iterator__ex2g4s.h()) {
    var elem = _iterator__ex2g4s.i();
    var elemOther = otherIterator.i();
    if (!equals(elem, elemOther)) {
      return false;
    }
  }
  return true;
};
var Companion_instance_5;
function Companion_getInstance_5() {
  return Companion_instance_5;
}
function AbstractList() {
  AbstractCollection.call(this);
}
protoOf(AbstractList).g = function () {
  return new IteratorImpl_0(this);
};
protoOf(AbstractList).equals = function (other) {
  if (other === this)
    return true;
  if (!(!(other == null) ? isInterface(other, KtList) : false))
    return false;
  return Companion_instance_5.p2(this, other);
};
protoOf(AbstractList).hashCode = function () {
  return Companion_instance_5.q2(this);
};
function AbstractMap$values$1$iterator$1($entryIterator) {
  this.h8_1 = $entryIterator;
}
protoOf(AbstractMap$values$1$iterator$1).h = function () {
  return this.h8_1.h();
};
protoOf(AbstractMap$values$1$iterator$1).i = function () {
  return this.h8_1.i().k1();
};
function toString_2($this, entry) {
  return toString_3($this, entry.j1()) + '=' + toString_3($this, entry.k1());
}
function toString_3($this, o) {
  return o === $this ? '(this Map)' : toString_0(o);
}
function implFindEntry($this, key) {
  var tmp0 = $this.p1();
  var tmp$ret$0;
  $l$block: {
    // Inline function 'kotlin.collections.firstOrNull' call
    var _iterator__ex2g4s = tmp0.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if (equals(element.j1(), key)) {
        tmp$ret$0 = element;
        break $l$block;
      }
    }
    tmp$ret$0 = null;
  }
  return tmp$ret$0;
}
function Companion_6() {
}
var Companion_instance_6;
function Companion_getInstance_6() {
  return Companion_instance_6;
}
function AbstractMap$toString$lambda(this$0) {
  return function (it) {
    return toString_2(this$0, it);
  };
}
function AbstractMap$values$1(this$0) {
  this.i8_1 = this$0;
  AbstractCollection.call(this);
}
protoOf(AbstractMap$values$1).o3 = function (element) {
  return this.i8_1.m1(element);
};
protoOf(AbstractMap$values$1).h1 = function (element) {
  if (!true)
    return false;
  return this.o3(element);
};
protoOf(AbstractMap$values$1).g = function () {
  var entryIterator = this.i8_1.p1().g();
  return new AbstractMap$values$1$iterator$1(entryIterator);
};
protoOf(AbstractMap$values$1).j = function () {
  return this.i8_1.j();
};
function AbstractMap() {
  this.x2_1 = null;
  this.y2_1 = null;
}
protoOf(AbstractMap).l1 = function (key) {
  return !(implFindEntry(this, key) == null);
};
protoOf(AbstractMap).m1 = function (value) {
  var tmp0 = this.p1();
  var tmp$ret$0;
  $l$block_0: {
    // Inline function 'kotlin.collections.any' call
    var tmp;
    if (isInterface(tmp0, Collection)) {
      tmp = tmp0.l();
    } else {
      tmp = false;
    }
    if (tmp) {
      tmp$ret$0 = false;
      break $l$block_0;
    }
    var _iterator__ex2g4s = tmp0.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if (equals(element.k1(), value)) {
        tmp$ret$0 = true;
        break $l$block_0;
      }
    }
    tmp$ret$0 = false;
  }
  return tmp$ret$0;
};
protoOf(AbstractMap).z2 = function (entry) {
  if (!(!(entry == null) ? isInterface(entry, Entry) : false))
    return false;
  var key = entry.j1();
  var value = entry.k1();
  // Inline function 'kotlin.collections.get' call
  var ourValue = (isInterface(this, KtMap) ? this : THROW_CCE()).n1(key);
  if (!equals(value, ourValue)) {
    return false;
  }
  var tmp;
  if (ourValue == null) {
    // Inline function 'kotlin.collections.containsKey' call
    tmp = !(isInterface(this, KtMap) ? this : THROW_CCE()).l1(key);
  } else {
    tmp = false;
  }
  if (tmp) {
    return false;
  }
  return true;
};
protoOf(AbstractMap).equals = function (other) {
  if (other === this)
    return true;
  if (!(!(other == null) ? isInterface(other, KtMap) : false))
    return false;
  if (!(this.j() === other.j()))
    return false;
  var tmp0 = other.p1();
  var tmp$ret$0;
  $l$block_0: {
    // Inline function 'kotlin.collections.all' call
    var tmp;
    if (isInterface(tmp0, Collection)) {
      tmp = tmp0.l();
    } else {
      tmp = false;
    }
    if (tmp) {
      tmp$ret$0 = true;
      break $l$block_0;
    }
    var _iterator__ex2g4s = tmp0.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if (!this.z2(element)) {
        tmp$ret$0 = false;
        break $l$block_0;
      }
    }
    tmp$ret$0 = true;
  }
  return tmp$ret$0;
};
protoOf(AbstractMap).n1 = function (key) {
  var tmp0_safe_receiver = implFindEntry(this, key);
  return tmp0_safe_receiver == null ? null : tmp0_safe_receiver.k1();
};
protoOf(AbstractMap).hashCode = function () {
  return hashCode_0(this.p1());
};
protoOf(AbstractMap).l = function () {
  return this.j() === 0;
};
protoOf(AbstractMap).j = function () {
  return this.p1().j();
};
protoOf(AbstractMap).toString = function () {
  var tmp = this.p1();
  return joinToString_0(tmp, ', ', '{', '}', VOID, VOID, AbstractMap$toString$lambda(this));
};
protoOf(AbstractMap).o1 = function () {
  if (this.y2_1 == null) {
    var tmp = this;
    tmp.y2_1 = new AbstractMap$values$1(this);
  }
  return ensureNotNull(this.y2_1);
};
function Companion_7() {
}
protoOf(Companion_7).b3 = function (c) {
  var hashCode = 0;
  var _iterator__ex2g4s = c.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    var tmp = hashCode;
    var tmp1_elvis_lhs = element == null ? null : hashCode_0(element);
    hashCode = tmp + (tmp1_elvis_lhs == null ? 0 : tmp1_elvis_lhs) | 0;
  }
  return hashCode;
};
protoOf(Companion_7).a3 = function (c, other) {
  if (!(c.j() === other.j()))
    return false;
  return c.i1(other);
};
var Companion_instance_7;
function Companion_getInstance_7() {
  return Companion_instance_7;
}
function ArrayDeque_init_$Init$($this) {
  AbstractMutableList.call($this);
  ArrayDeque.call($this);
  $this.l8_1 = Companion_getInstance_8().n8_1;
  return $this;
}
function ArrayDeque_init_$Create$() {
  return ArrayDeque_init_$Init$(objectCreate(protoOf(ArrayDeque)));
}
function ensureCapacity_0($this, minCapacity) {
  if (minCapacity < 0)
    throw IllegalStateException_init_$Create$_0('Deque is too big.');
  if (minCapacity <= $this.l8_1.length)
    return Unit_instance;
  if ($this.l8_1 === Companion_getInstance_8().n8_1) {
    var tmp = $this;
    // Inline function 'kotlin.arrayOfNulls' call
    var size = coerceAtLeast(minCapacity, 10);
    tmp.l8_1 = Array(size);
    return Unit_instance;
  }
  var newCapacity = Companion_instance_5.r4($this.l8_1.length, minCapacity);
  copyElements($this, newCapacity);
}
function copyElements($this, newCapacity) {
  // Inline function 'kotlin.arrayOfNulls' call
  var newElements = Array(newCapacity);
  var tmp0 = $this.l8_1;
  var tmp6 = $this.k8_1;
  // Inline function 'kotlin.collections.copyInto' call
  var endIndex = $this.l8_1.length;
  arrayCopy(tmp0, newElements, 0, tmp6, endIndex);
  var tmp0_0 = $this.l8_1;
  var tmp4 = $this.l8_1.length - $this.k8_1 | 0;
  // Inline function 'kotlin.collections.copyInto' call
  var endIndex_0 = $this.k8_1;
  arrayCopy(tmp0_0, newElements, tmp4, 0, endIndex_0);
  $this.k8_1 = 0;
  $this.l8_1 = newElements;
}
function positiveMod($this, index) {
  return index >= $this.l8_1.length ? index - $this.l8_1.length | 0 : index;
}
function decremented($this, index) {
  return index === 0 ? get_lastIndex($this.l8_1) : index - 1 | 0;
}
function copyCollectionElements($this, internalIndex, elements) {
  var iterator = elements.g();
  var inductionVariable = internalIndex;
  var last = $this.l8_1.length;
  if (inductionVariable < last)
    $l$loop: do {
      var index = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      if (!iterator.h())
        break $l$loop;
      $this.l8_1[index] = iterator.i();
    }
     while (inductionVariable < last);
  var inductionVariable_0 = 0;
  var last_0 = $this.k8_1;
  if (inductionVariable_0 < last_0)
    $l$loop_0: do {
      var index_0 = inductionVariable_0;
      inductionVariable_0 = inductionVariable_0 + 1 | 0;
      if (!iterator.h())
        break $l$loop_0;
      $this.l8_1[index_0] = iterator.i();
    }
     while (inductionVariable_0 < last_0);
  $this.m8_1 = $this.m8_1 + elements.j() | 0;
}
function registerModification_0($this) {
  $this.m2_1 = $this.m2_1 + 1 | 0;
}
function Companion_8() {
  Companion_instance_8 = this;
  var tmp = this;
  // Inline function 'kotlin.emptyArray' call
  tmp.n8_1 = [];
  this.o8_1 = 10;
}
var Companion_instance_8;
function Companion_getInstance_8() {
  if (Companion_instance_8 == null)
    new Companion_8();
  return Companion_instance_8;
}
protoOf(ArrayDeque).j = function () {
  return this.m8_1;
};
protoOf(ArrayDeque).l = function () {
  return this.m8_1 === 0;
};
protoOf(ArrayDeque).p8 = function (element) {
  registerModification_0(this);
  ensureCapacity_0(this, this.m8_1 + 1 | 0);
  this.k8_1 = decremented(this, this.k8_1);
  this.l8_1[this.k8_1] = element;
  this.m8_1 = this.m8_1 + 1 | 0;
};
protoOf(ArrayDeque).q8 = function (element) {
  registerModification_0(this);
  ensureCapacity_0(this, this.m8_1 + 1 | 0);
  var tmp = this.l8_1;
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  var index = this.m8_1;
  tmp[positiveMod(this, this.k8_1 + index | 0)] = element;
  this.m8_1 = this.m8_1 + 1 | 0;
};
protoOf(ArrayDeque).r8 = function () {
  if (this.l())
    throw NoSuchElementException_init_$Create$_0('ArrayDeque is empty.');
  registerModification_0(this);
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  var index = get_lastIndex_0(this);
  var internalLastIndex = positiveMod(this, this.k8_1 + index | 0);
  // Inline function 'kotlin.collections.ArrayDeque.internalGet' call
  var element = this.l8_1[internalLastIndex];
  this.l8_1[internalLastIndex] = null;
  this.m8_1 = this.m8_1 - 1 | 0;
  return element;
};
protoOf(ArrayDeque).f = function (element) {
  this.q8(element);
  return true;
};
protoOf(ArrayDeque).n2 = function (index, element) {
  Companion_instance_5.e3(index, this.m8_1);
  if (index === this.m8_1) {
    this.q8(element);
    return Unit_instance;
  } else if (index === 0) {
    this.p8(element);
    return Unit_instance;
  }
  registerModification_0(this);
  ensureCapacity_0(this, this.m8_1 + 1 | 0);
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  var internalIndex = positiveMod(this, this.k8_1 + index | 0);
  if (index < (this.m8_1 + 1 | 0) >> 1) {
    var decrementedInternalIndex = decremented(this, internalIndex);
    var decrementedHead = decremented(this, this.k8_1);
    if (decrementedInternalIndex >= this.k8_1) {
      this.l8_1[decrementedHead] = this.l8_1[this.k8_1];
      var tmp0 = this.l8_1;
      var tmp2 = this.l8_1;
      var tmp4 = this.k8_1;
      var tmp6 = this.k8_1 + 1 | 0;
      // Inline function 'kotlin.collections.copyInto' call
      var endIndex = decrementedInternalIndex + 1 | 0;
      arrayCopy(tmp0, tmp2, tmp4, tmp6, endIndex);
    } else {
      var tmp0_0 = this.l8_1;
      var tmp2_0 = this.l8_1;
      var tmp4_0 = this.k8_1 - 1 | 0;
      var tmp6_0 = this.k8_1;
      // Inline function 'kotlin.collections.copyInto' call
      var endIndex_0 = this.l8_1.length;
      arrayCopy(tmp0_0, tmp2_0, tmp4_0, tmp6_0, endIndex_0);
      this.l8_1[this.l8_1.length - 1 | 0] = this.l8_1[0];
      var tmp0_1 = this.l8_1;
      var tmp2_1 = this.l8_1;
      // Inline function 'kotlin.collections.copyInto' call
      var endIndex_1 = decrementedInternalIndex + 1 | 0;
      arrayCopy(tmp0_1, tmp2_1, 0, 1, endIndex_1);
    }
    this.l8_1[decrementedInternalIndex] = element;
    this.k8_1 = decrementedHead;
  } else {
    // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
    var index_0 = this.m8_1;
    var tail = positiveMod(this, this.k8_1 + index_0 | 0);
    if (internalIndex < tail) {
      var tmp0_2 = this.l8_1;
      var tmp2_2 = this.l8_1;
      // Inline function 'kotlin.collections.copyInto' call
      var destinationOffset = internalIndex + 1 | 0;
      arrayCopy(tmp0_2, tmp2_2, destinationOffset, internalIndex, tail);
    } else {
      var tmp0_3 = this.l8_1;
      // Inline function 'kotlin.collections.copyInto' call
      var destination = this.l8_1;
      arrayCopy(tmp0_3, destination, 1, 0, tail);
      this.l8_1[0] = this.l8_1[this.l8_1.length - 1 | 0];
      var tmp0_4 = this.l8_1;
      var tmp2_3 = this.l8_1;
      var tmp4_1 = internalIndex + 1 | 0;
      // Inline function 'kotlin.collections.copyInto' call
      var endIndex_2 = this.l8_1.length - 1 | 0;
      arrayCopy(tmp0_4, tmp2_3, tmp4_1, internalIndex, endIndex_2);
    }
    this.l8_1[internalIndex] = element;
  }
  this.m8_1 = this.m8_1 + 1 | 0;
};
protoOf(ArrayDeque).p = function (elements) {
  if (elements.l())
    return false;
  registerModification_0(this);
  ensureCapacity_0(this, this.m8_1 + elements.j() | 0);
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  var index = this.m8_1;
  var tmp$ret$0 = positiveMod(this, this.k8_1 + index | 0);
  copyCollectionElements(this, tmp$ret$0, elements);
  return true;
};
protoOf(ArrayDeque).k = function (index) {
  Companion_instance_5.d3(index, this.m8_1);
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  // Inline function 'kotlin.collections.ArrayDeque.internalGet' call
  var internalIndex = positiveMod(this, this.k8_1 + index | 0);
  return this.l8_1[internalIndex];
};
protoOf(ArrayDeque).h2 = function (index, element) {
  Companion_instance_5.d3(index, this.m8_1);
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  var internalIndex = positiveMod(this, this.k8_1 + index | 0);
  // Inline function 'kotlin.collections.ArrayDeque.internalGet' call
  var oldElement = this.l8_1[internalIndex];
  this.l8_1[internalIndex] = element;
  return oldElement;
};
protoOf(ArrayDeque).h1 = function (element) {
  return !(this.o2(element) === -1);
};
protoOf(ArrayDeque).o2 = function (element) {
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  var index = this.m8_1;
  var tail = positiveMod(this, this.k8_1 + index | 0);
  if (this.k8_1 < tail) {
    var inductionVariable = this.k8_1;
    if (inductionVariable < tail)
      do {
        var index_0 = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        if (equals(element, this.l8_1[index_0]))
          return index_0 - this.k8_1 | 0;
      }
       while (inductionVariable < tail);
  } else {
    var tmp;
    // Inline function 'kotlin.collections.isNotEmpty' call
    if (!this.l()) {
      tmp = this.k8_1 >= tail;
    } else {
      tmp = false;
    }
    if (tmp) {
      var inductionVariable_0 = this.k8_1;
      var last = this.l8_1.length;
      if (inductionVariable_0 < last)
        do {
          var index_1 = inductionVariable_0;
          inductionVariable_0 = inductionVariable_0 + 1 | 0;
          if (equals(element, this.l8_1[index_1]))
            return index_1 - this.k8_1 | 0;
        }
         while (inductionVariable_0 < last);
      var inductionVariable_1 = 0;
      if (inductionVariable_1 < tail)
        do {
          var index_2 = inductionVariable_1;
          inductionVariable_1 = inductionVariable_1 + 1 | 0;
          if (equals(element, this.l8_1[index_2]))
            return (index_2 + this.l8_1.length | 0) - this.k8_1 | 0;
        }
         while (inductionVariable_1 < tail);
    }
  }
  return -1;
};
protoOf(ArrayDeque).s8 = function (array) {
  var tmp = array.length >= this.m8_1 ? array : arrayOfNulls(array, this.m8_1);
  var dest = isArray(tmp) ? tmp : THROW_CCE();
  // Inline function 'kotlin.collections.ArrayDeque.internalIndex' call
  var index = this.m8_1;
  var tail = positiveMod(this, this.k8_1 + index | 0);
  if (this.k8_1 < tail) {
    var tmp0 = this.l8_1;
    // Inline function 'kotlin.collections.copyInto' call
    var startIndex = this.k8_1;
    arrayCopy(tmp0, dest, 0, startIndex, tail);
  } else {
    // Inline function 'kotlin.collections.isNotEmpty' call
    if (!this.l()) {
      var tmp0_0 = this.l8_1;
      var tmp6 = this.k8_1;
      // Inline function 'kotlin.collections.copyInto' call
      var endIndex = this.l8_1.length;
      arrayCopy(tmp0_0, dest, 0, tmp6, endIndex);
      var tmp0_1 = this.l8_1;
      // Inline function 'kotlin.collections.copyInto' call
      var destinationOffset = this.l8_1.length - this.k8_1 | 0;
      arrayCopy(tmp0_1, dest, destinationOffset, 0, tail);
    }
  }
  var tmp_0 = terminateCollectionToArray(this.m8_1, dest);
  return isArray(tmp_0) ? tmp_0 : THROW_CCE();
};
protoOf(ArrayDeque).f3 = function () {
  // Inline function 'kotlin.arrayOfNulls' call
  var size = this.m8_1;
  var tmp$ret$0 = Array(size);
  return this.s8(tmp$ret$0);
};
protoOf(ArrayDeque).toArray = function () {
  return this.f3();
};
function ArrayDeque() {
  Companion_getInstance_8();
  this.k8_1 = 0;
  this.m8_1 = 0;
}
function get_lastIndex_0(_this__u8e3s4) {
  return _this__u8e3s4.j() - 1 | 0;
}
function throwIndexOverflow() {
  throw ArithmeticException_init_$Create$_0('Index overflow has happened.');
}
function collectionToArrayCommonImpl(collection) {
  if (collection.l()) {
    // Inline function 'kotlin.emptyArray' call
    return [];
  }
  // Inline function 'kotlin.arrayOfNulls' call
  var size = collection.j();
  var destination = Array(size);
  var iterator = collection.g();
  var index = 0;
  while (iterator.h()) {
    var _unary__edvuaz = index;
    index = _unary__edvuaz + 1 | 0;
    destination[_unary__edvuaz] = iterator.i();
  }
  return destination;
}
function listOf_0(elements) {
  return elements.length > 0 ? asList(elements) : emptyList();
}
function get_indices(_this__u8e3s4) {
  return numberRangeToNumber(0, _this__u8e3s4.j() - 1 | 0);
}
function emptyList() {
  return EmptyList_getInstance();
}
function EmptyList() {
  EmptyList_instance = this;
  this.t8_1 = new Long(-1478467534, -1720727600);
}
protoOf(EmptyList).equals = function (other) {
  var tmp;
  if (!(other == null) ? isInterface(other, KtList) : false) {
    tmp = other.l();
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(EmptyList).hashCode = function () {
  return 1;
};
protoOf(EmptyList).toString = function () {
  return '[]';
};
protoOf(EmptyList).j = function () {
  return 0;
};
protoOf(EmptyList).l = function () {
  return true;
};
protoOf(EmptyList).u8 = function (element) {
  return false;
};
protoOf(EmptyList).h1 = function (element) {
  if (!false)
    return false;
  var tmp;
  if (false) {
    tmp = element;
  } else {
    tmp = THROW_CCE();
  }
  return this.u8(tmp);
};
protoOf(EmptyList).k = function (index) {
  throw IndexOutOfBoundsException_init_$Create$_0("Empty list doesn't contain element at index " + index + '.');
};
protoOf(EmptyList).g = function () {
  return EmptyIterator_instance;
};
var EmptyList_instance;
function EmptyList_getInstance() {
  if (EmptyList_instance == null)
    new EmptyList();
  return EmptyList_instance;
}
function EmptyIterator() {
}
protoOf(EmptyIterator).h = function () {
  return false;
};
protoOf(EmptyIterator).i = function () {
  throw NoSuchElementException_init_$Create$();
};
var EmptyIterator_instance;
function EmptyIterator_getInstance() {
  return EmptyIterator_instance;
}
function optimizeReadOnlyList(_this__u8e3s4) {
  switch (_this__u8e3s4.j()) {
    case 0:
      return emptyList();
    case 1:
      return listOf(_this__u8e3s4.k(0));
    default:
      return _this__u8e3s4;
  }
}
function collectionSizeOrDefault(_this__u8e3s4, default_0) {
  var tmp;
  if (isInterface(_this__u8e3s4, Collection)) {
    tmp = _this__u8e3s4.j();
  } else {
    tmp = default_0;
  }
  return tmp;
}
function getOrImplicitDefault(_this__u8e3s4, key) {
  if (isInterface(_this__u8e3s4, MapWithDefault))
    return _this__u8e3s4.v8(key);
  var tmp$ret$0;
  $l$block_0: {
    // Inline function 'kotlin.collections.getOrElseIfMissing' call
    var value = _this__u8e3s4.n1(key);
    if (value == null && !_this__u8e3s4.l1(key)) {
      throw NoSuchElementException_init_$Create$_0('Key ' + toString_0(key) + ' is missing in the map.');
    } else {
      tmp$ret$0 = value;
      break $l$block_0;
    }
  }
  return tmp$ret$0;
}
function MapWithDefault() {
}
function mapOf(pairs) {
  return pairs.length > 0 ? toMap(pairs, LinkedHashMap_init_$Create$_0(mapCapacity(pairs.length))) : emptyMap();
}
function getValue(_this__u8e3s4, key) {
  return getOrImplicitDefault(_this__u8e3s4, key);
}
function toMap(_this__u8e3s4, destination) {
  // Inline function 'kotlin.apply' call
  putAll(destination, _this__u8e3s4);
  return destination;
}
function emptyMap() {
  var tmp = EmptyMap_getInstance();
  return isInterface(tmp, KtMap) ? tmp : THROW_CCE();
}
function putAll(_this__u8e3s4, pairs) {
  var inductionVariable = 0;
  var last = pairs.length;
  while (inductionVariable < last) {
    var _destruct__k2r9zo = pairs[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    var key = _destruct__k2r9zo.y8();
    var value = _destruct__k2r9zo.z8();
    _this__u8e3s4.w2(key, value);
  }
}
function EmptyMap() {
  EmptyMap_instance = this;
  this.a9_1 = new Long(-888910638, 1920087921);
}
protoOf(EmptyMap).equals = function (other) {
  var tmp;
  if (!(other == null) ? isInterface(other, KtMap) : false) {
    tmp = other.l();
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(EmptyMap).hashCode = function () {
  return 0;
};
protoOf(EmptyMap).toString = function () {
  return '{}';
};
protoOf(EmptyMap).j = function () {
  return 0;
};
protoOf(EmptyMap).l = function () {
  return true;
};
protoOf(EmptyMap).b9 = function (key) {
  return false;
};
protoOf(EmptyMap).l1 = function (key) {
  if (!true)
    return false;
  return this.b9(key);
};
protoOf(EmptyMap).c9 = function (key) {
  return null;
};
protoOf(EmptyMap).n1 = function (key) {
  if (!true)
    return null;
  return this.c9(key);
};
protoOf(EmptyMap).p1 = function () {
  return EmptySet_getInstance();
};
protoOf(EmptyMap).o1 = function () {
  return EmptyList_getInstance();
};
var EmptyMap_instance;
function EmptyMap_getInstance() {
  if (EmptyMap_instance == null)
    new EmptyMap();
  return EmptyMap_instance;
}
function addAll(_this__u8e3s4, elements) {
  if (isInterface(elements, Collection))
    return _this__u8e3s4.p(elements);
  else {
    var result = false;
    var _iterator__ex2g4s = elements.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      if (_this__u8e3s4.f(item))
        result = true;
    }
    return result;
  }
}
function IntIterator() {
}
protoOf(IntIterator).i = function () {
  return this.d9();
};
function generateSequence(seedFunction, nextFunction) {
  return new GeneratorSequence(seedFunction, nextFunction);
}
function emptySequence() {
  return EmptySequence_instance;
}
function DropTakeSequence() {
}
function TakeSequence$iterator$1(this$0) {
  this.e9_1 = this$0.h9_1;
  this.f9_1 = this$0.g9_1.g();
}
protoOf(TakeSequence$iterator$1).i = function () {
  if (this.e9_1 === 0)
    throw NoSuchElementException_init_$Create$();
  this.e9_1 = this.e9_1 - 1 | 0;
  return this.f9_1.i();
};
protoOf(TakeSequence$iterator$1).h = function () {
  return this.e9_1 > 0 && this.f9_1.h();
};
function TakeSequence(sequence, count) {
  this.g9_1 = sequence;
  this.h9_1 = count;
  // Inline function 'kotlin.require' call
  if (!(this.h9_1 >= 0)) {
    var message = 'count must be non-negative, but was ' + this.h9_1 + '.';
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
}
protoOf(TakeSequence).t = function (n) {
  return n >= this.h9_1 ? this : new TakeSequence(this.g9_1, n);
};
protoOf(TakeSequence).g = function () {
  return new TakeSequence$iterator$1(this);
};
function TransformingSequence$iterator$1(this$0) {
  this.j9_1 = this$0;
  this.i9_1 = this$0.k9_1.g();
}
protoOf(TransformingSequence$iterator$1).i = function () {
  return this.j9_1.l9_1(this.i9_1.i());
};
protoOf(TransformingSequence$iterator$1).h = function () {
  return this.i9_1.h();
};
function TransformingSequence(sequence, transformer) {
  this.k9_1 = sequence;
  this.l9_1 = transformer;
}
protoOf(TransformingSequence).g = function () {
  return new TransformingSequence$iterator$1(this);
};
function calcNext($this) {
  $this.m9_1 = $this.n9_1 === -2 ? $this.o9_1.p9_1() : $this.o9_1.q9_1(ensureNotNull($this.m9_1));
  $this.n9_1 = $this.m9_1 == null ? 0 : 1;
}
function GeneratorSequence$iterator$1(this$0) {
  this.o9_1 = this$0;
  this.m9_1 = null;
  this.n9_1 = -2;
}
protoOf(GeneratorSequence$iterator$1).i = function () {
  if (this.n9_1 < 0) {
    calcNext(this);
  }
  if (this.n9_1 === 0)
    throw NoSuchElementException_init_$Create$();
  var tmp = this.m9_1;
  var result = !(tmp == null) ? tmp : THROW_CCE();
  this.n9_1 = -1;
  return result;
};
protoOf(GeneratorSequence$iterator$1).h = function () {
  if (this.n9_1 < 0) {
    calcNext(this);
  }
  return this.n9_1 === 1;
};
function GeneratorSequence(getInitialValue, getNextValue) {
  this.p9_1 = getInitialValue;
  this.q9_1 = getNextValue;
}
protoOf(GeneratorSequence).g = function () {
  return new GeneratorSequence$iterator$1(this);
};
function EmptySequence() {
}
protoOf(EmptySequence).g = function () {
  return EmptyIterator_instance;
};
protoOf(EmptySequence).t = function (n) {
  return EmptySequence_instance;
};
var EmptySequence_instance;
function EmptySequence_getInstance() {
  return EmptySequence_instance;
}
function emptySet() {
  return EmptySet_getInstance();
}
function optimizeReadOnlySet(_this__u8e3s4) {
  switch (_this__u8e3s4.j()) {
    case 0:
      return emptySet();
    case 1:
      return setOf(_this__u8e3s4.g().i());
    default:
      return _this__u8e3s4;
  }
}
function EmptySet() {
  EmptySet_instance = this;
  this.r9_1 = new Long(1993859828, 793161749);
}
protoOf(EmptySet).equals = function (other) {
  var tmp;
  if (!(other == null) ? isInterface(other, KtSet) : false) {
    tmp = other.l();
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(EmptySet).hashCode = function () {
  return 0;
};
protoOf(EmptySet).toString = function () {
  return '[]';
};
protoOf(EmptySet).j = function () {
  return 0;
};
protoOf(EmptySet).l = function () {
  return true;
};
protoOf(EmptySet).u8 = function (element) {
  return false;
};
protoOf(EmptySet).h1 = function (element) {
  if (!false)
    return false;
  var tmp;
  if (false) {
    tmp = element;
  } else {
    tmp = THROW_CCE();
  }
  return this.u8(tmp);
};
protoOf(EmptySet).s9 = function (elements) {
  return elements.l();
};
protoOf(EmptySet).i1 = function (elements) {
  return this.s9(elements);
};
protoOf(EmptySet).g = function () {
  return EmptyIterator_instance;
};
var EmptySet_instance;
function EmptySet_getInstance() {
  if (EmptySet_instance == null)
    new EmptySet();
  return EmptySet_instance;
}
function hashSetOf(elements) {
  return toCollection(elements, HashSet_init_$Create$_0(mapCapacity(elements.length)));
}
function naturalOrder() {
  var tmp = NaturalOrderComparator_instance;
  return isInterface(tmp, Comparator) ? tmp : THROW_CCE();
}
function NaturalOrderComparator() {
}
protoOf(NaturalOrderComparator).t9 = function (a, b) {
  return compareTo(a, b);
};
protoOf(NaturalOrderComparator).compare = function (a, b) {
  var tmp = (!(a == null) ? isComparable(a) : false) ? a : THROW_CCE();
  return this.t9(tmp, (!(b == null) ? isComparable(b) : false) ? b : THROW_CCE());
};
var NaturalOrderComparator_instance;
function NaturalOrderComparator_getInstance() {
  return NaturalOrderComparator_instance;
}
function getProgressionLastElement(start, end, step) {
  var tmp;
  if (step > 0) {
    tmp = start >= end ? end : end - differenceModulo(end, start, step) | 0;
  } else if (step < 0) {
    tmp = start <= end ? end : end + differenceModulo(start, end, -step | 0) | 0;
  } else {
    throw IllegalArgumentException_init_$Create$_0('Step is zero.');
  }
  return tmp;
}
function differenceModulo(a, b, c) {
  return mod(mod(a, c) - mod(b, c) | 0, c);
}
function mod(a, b) {
  var mod = a % b | 0;
  return mod >= 0 ? mod : mod + b | 0;
}
function Companion_9() {
  Companion_instance_9 = this;
  this.r_1 = new IntRange(1, 0);
}
var Companion_instance_9;
function Companion_getInstance_9() {
  if (Companion_instance_9 == null) {
    Companion_instance_10;
    new Companion_9();
  }
  return Companion_instance_9;
}
function IntRange(start, endInclusive) {
  Companion_getInstance_9();
  IntProgression.call(this, start, endInclusive, 1);
}
protoOf(IntRange).j7 = function () {
  return this.u9_1;
};
protoOf(IntRange).k7 = function () {
  return this.v9_1;
};
protoOf(IntRange).l = function () {
  return this.u9_1 > this.v9_1;
};
protoOf(IntRange).equals = function (other) {
  var tmp;
  if (other instanceof IntRange) {
    tmp = this.l() && other.l() || (this.u9_1 === other.u9_1 && this.v9_1 === other.v9_1);
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(IntRange).hashCode = function () {
  return this.l() ? -1 : imul_0(31, this.u9_1) + this.v9_1 | 0;
};
protoOf(IntRange).toString = function () {
  return '' + this.u9_1 + '..' + this.v9_1;
};
function IntProgressionIterator(first, last, step) {
  IntIterator.call(this);
  this.x9_1 = step;
  this.y9_1 = last;
  this.z9_1 = this.x9_1 > 0 ? first <= last : first >= last;
  this.aa_1 = this.z9_1 ? first : this.y9_1;
}
protoOf(IntProgressionIterator).h = function () {
  return this.z9_1;
};
protoOf(IntProgressionIterator).d9 = function () {
  var value = this.aa_1;
  if (value === this.y9_1) {
    if (!this.z9_1)
      throw NoSuchElementException_init_$Create$();
    this.z9_1 = false;
  } else {
    this.aa_1 = this.aa_1 + this.x9_1 | 0;
  }
  return value;
};
function Companion_10() {
}
protoOf(Companion_10).s = function (rangeStart, rangeEnd, step) {
  return new IntProgression(rangeStart, rangeEnd, step);
};
var Companion_instance_10;
function Companion_getInstance_10() {
  return Companion_instance_10;
}
function IntProgression(start, endInclusive, step) {
  if (step === 0)
    throw IllegalArgumentException_init_$Create$_0('Step must be non-zero.');
  if (step === -2147483648)
    throw IllegalArgumentException_init_$Create$_0('Step must be greater than Int.MIN_VALUE to avoid overflow on negation.');
  this.u9_1 = start;
  this.v9_1 = getProgressionLastElement(start, endInclusive, step);
  this.w9_1 = step;
}
protoOf(IntProgression).g = function () {
  return new IntProgressionIterator(this.u9_1, this.v9_1, this.w9_1);
};
protoOf(IntProgression).l = function () {
  return this.w9_1 > 0 ? this.u9_1 > this.v9_1 : this.u9_1 < this.v9_1;
};
protoOf(IntProgression).equals = function (other) {
  var tmp;
  if (other instanceof IntProgression) {
    tmp = this.l() && other.l() || (this.u9_1 === other.u9_1 && this.v9_1 === other.v9_1 && this.w9_1 === other.w9_1);
  } else {
    tmp = false;
  }
  return tmp;
};
protoOf(IntProgression).hashCode = function () {
  return this.l() ? -1 : imul_0(31, imul_0(31, this.u9_1) + this.v9_1 | 0) + this.w9_1 | 0;
};
protoOf(IntProgression).toString = function () {
  return this.w9_1 > 0 ? '' + this.u9_1 + '..' + this.v9_1 + ' step ' + this.w9_1 : '' + this.u9_1 + ' downTo ' + this.v9_1 + ' step ' + (-this.w9_1 | 0);
};
function appendElement(_this__u8e3s4, element, transform) {
  if (!(transform == null))
    _this__u8e3s4.e(transform(element));
  else {
    if (element == null ? true : isCharSequence(element))
      _this__u8e3s4.e(element);
    else {
      if (element instanceof Char)
        _this__u8e3s4.t5(element.v_1);
      else {
        _this__u8e3s4.e(toString_1(element));
      }
    }
  }
}
function equals_0(_this__u8e3s4, other, ignoreCase) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  if (_this__u8e3s4 === other)
    return true;
  if (!ignoreCase)
    return false;
  var thisUpper = uppercaseChar(_this__u8e3s4);
  var otherUpper = uppercaseChar(other);
  var tmp;
  if (thisUpper === otherUpper) {
    tmp = true;
  } else {
    // Inline function 'kotlin.text.lowercaseChar' call
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    var tmp$ret$1 = toString(thisUpper).toLowerCase();
    var tmp_0 = charCodeAt(tmp$ret$1, 0);
    // Inline function 'kotlin.text.lowercaseChar' call
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    var tmp$ret$5 = toString(otherUpper).toLowerCase();
    tmp = tmp_0 === charCodeAt(tmp$ret$5, 0);
  }
  return tmp;
}
function toIntOrNull(_this__u8e3s4) {
  return toIntOrNull_0(_this__u8e3s4, 10);
}
function toIntOrNull_0(_this__u8e3s4, radix) {
  checkRadix(radix);
  var length = _this__u8e3s4.length;
  if (length === 0)
    return null;
  var start;
  var isNegative;
  var limit;
  var firstChar = charCodeAt(_this__u8e3s4, 0);
  if (Char__compareTo_impl_ypi4mb(firstChar, _Char___init__impl__6a9atx(48)) < 0) {
    if (length === 1)
      return null;
    start = 1;
    if (firstChar === _Char___init__impl__6a9atx(45)) {
      isNegative = true;
      limit = -2147483648;
    } else if (firstChar === _Char___init__impl__6a9atx(43)) {
      isNegative = false;
      limit = -2147483647;
    } else
      return null;
  } else {
    start = 0;
    isNegative = false;
    limit = -2147483647;
  }
  var limitForMaxRadix = -59652323;
  var limitBeforeMul = limitForMaxRadix;
  var result = 0;
  var inductionVariable = start;
  if (inductionVariable < length)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      var digit = digitOf(charCodeAt(_this__u8e3s4, i), radix);
      if (digit < 0)
        return null;
      if (result < limitBeforeMul) {
        if (limitBeforeMul === limitForMaxRadix) {
          limitBeforeMul = limit / radix | 0;
          if (result < limitBeforeMul) {
            return null;
          }
        } else {
          return null;
        }
      }
      result = imul_0(result, radix);
      if (result < (limit + digit | 0))
        return null;
      result = result - digit | 0;
    }
     while (inductionVariable < length);
  return isNegative ? result : -result | 0;
}
function numberFormatError(input) {
  throw NumberFormatException_init_$Create$_0("Invalid number format: '" + input + "'");
}
function indexOf_1(_this__u8e3s4, char, startIndex, ignoreCase) {
  startIndex = startIndex === VOID ? 0 : startIndex;
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  var tmp;
  var tmp_0;
  if (ignoreCase) {
    tmp_0 = true;
  } else {
    tmp_0 = !(typeof _this__u8e3s4 === 'string');
  }
  if (tmp_0) {
    // Inline function 'kotlin.charArrayOf' call
    var tmp$ret$0 = charArrayOf([char]);
    tmp = indexOfAny(_this__u8e3s4, tmp$ret$0, startIndex, ignoreCase);
  } else {
    // Inline function 'kotlin.text.nativeIndexOf' call
    // Inline function 'kotlin.text.nativeIndexOf' call
    var str = toString(char);
    // Inline function 'kotlin.js.asDynamic' call
    tmp = _this__u8e3s4.indexOf(str, startIndex);
  }
  return tmp;
}
function contains_1(_this__u8e3s4, char, ignoreCase) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  return indexOf_1(_this__u8e3s4, char, VOID, ignoreCase) >= 0;
}
function split(_this__u8e3s4, delimiters, ignoreCase, limit) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  limit = limit === VOID ? 0 : limit;
  if (delimiters.length === 1) {
    return split_0(_this__u8e3s4, toString(delimiters[0]), ignoreCase, limit);
  }
  // Inline function 'kotlin.collections.map' call
  var this_0 = asIterable(rangesDelimitedBy(_this__u8e3s4, delimiters, VOID, ignoreCase, limit));
  // Inline function 'kotlin.collections.mapTo' call
  var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
  var _iterator__ex2g4s = this_0.g();
  while (_iterator__ex2g4s.h()) {
    var item = _iterator__ex2g4s.i();
    var tmp$ret$2 = substring_1(_this__u8e3s4, item);
    destination.f(tmp$ret$2);
  }
  return destination;
}
function isBlank(_this__u8e3s4) {
  var tmp$ret$0;
  $l$block: {
    // Inline function 'kotlin.text.all' call
    var inductionVariable = 0;
    while (inductionVariable < charSequenceLength(_this__u8e3s4)) {
      var element = charSequenceGet(_this__u8e3s4, inductionVariable);
      inductionVariable = inductionVariable + 1 | 0;
      if (!isWhitespace(element)) {
        tmp$ret$0 = false;
        break $l$block;
      }
    }
    tmp$ret$0 = true;
  }
  return tmp$ret$0;
}
function trim(_this__u8e3s4) {
  // Inline function 'kotlin.text.trim' call
  var startIndex = 0;
  var endIndex = charSequenceLength(_this__u8e3s4) - 1 | 0;
  var startFound = false;
  $l$loop: while (startIndex <= endIndex) {
    var index = !startFound ? startIndex : endIndex;
    var p0 = charSequenceGet(_this__u8e3s4, index);
    var match = isWhitespace(p0);
    if (!startFound) {
      if (!match)
        startFound = true;
      else
        startIndex = startIndex + 1 | 0;
    } else {
      if (!match)
        break $l$loop;
      else
        endIndex = endIndex - 1 | 0;
    }
  }
  return charSequenceSubSequence(_this__u8e3s4, startIndex, endIndex + 1 | 0);
}
function indexOfAny(_this__u8e3s4, chars, startIndex, ignoreCase) {
  startIndex = startIndex === VOID ? 0 : startIndex;
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  var tmp;
  if (!ignoreCase && chars.length === 1) {
    tmp = typeof _this__u8e3s4 === 'string';
  } else {
    tmp = false;
  }
  if (tmp) {
    var char = single(chars);
    // Inline function 'kotlin.text.nativeIndexOf' call
    // Inline function 'kotlin.text.nativeIndexOf' call
    var str = toString(char);
    // Inline function 'kotlin.js.asDynamic' call
    return _this__u8e3s4.indexOf(str, startIndex);
  }
  var inductionVariable = coerceAtLeast(startIndex, 0);
  var last = get_lastIndex_1(_this__u8e3s4);
  if (inductionVariable <= last)
    do {
      var index = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      var charAtIndex = charSequenceGet(_this__u8e3s4, index);
      var tmp$ret$3;
      $l$block: {
        // Inline function 'kotlin.collections.any' call
        var inductionVariable_0 = 0;
        var last_0 = chars.length;
        while (inductionVariable_0 < last_0) {
          var element = chars[inductionVariable_0];
          inductionVariable_0 = inductionVariable_0 + 1 | 0;
          if (equals_0(element, charAtIndex, ignoreCase)) {
            tmp$ret$3 = true;
            break $l$block;
          }
        }
        tmp$ret$3 = false;
      }
      if (tmp$ret$3)
        return index;
    }
     while (!(index === last));
  return -1;
}
function split_0(_this__u8e3s4, delimiter, ignoreCase, limit) {
  requireNonNegativeLimit(limit);
  var currentOffset = 0;
  var nextIndex = indexOf_2(_this__u8e3s4, delimiter, currentOffset, ignoreCase);
  if (nextIndex === -1 || limit === 1) {
    return listOf(toString_1(_this__u8e3s4));
  }
  var isLimited = limit > 0;
  var result = ArrayList_init_$Create$_0(isLimited ? coerceAtMost(limit, 10) : 10);
  $l$loop: do {
    var tmp2 = currentOffset;
    // Inline function 'kotlin.text.substring' call
    var endIndex = nextIndex;
    var tmp$ret$0 = toString_1(charSequenceSubSequence(_this__u8e3s4, tmp2, endIndex));
    result.f(tmp$ret$0);
    currentOffset = nextIndex + delimiter.length | 0;
    if (isLimited && result.j() === (limit - 1 | 0))
      break $l$loop;
    nextIndex = indexOf_2(_this__u8e3s4, delimiter, currentOffset, ignoreCase);
  }
   while (!(nextIndex === -1));
  var tmp2_0 = currentOffset;
  // Inline function 'kotlin.text.substring' call
  var endIndex_0 = charSequenceLength(_this__u8e3s4);
  var tmp$ret$1 = toString_1(charSequenceSubSequence(_this__u8e3s4, tmp2_0, endIndex_0));
  result.f(tmp$ret$1);
  return result;
}
function rangesDelimitedBy(_this__u8e3s4, delimiters, startIndex, ignoreCase, limit) {
  startIndex = startIndex === VOID ? 0 : startIndex;
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  limit = limit === VOID ? 0 : limit;
  requireNonNegativeLimit(limit);
  return new DelimitedRangesSequence(_this__u8e3s4, startIndex, limit, rangesDelimitedBy$lambda(delimiters, ignoreCase));
}
function substring_1(_this__u8e3s4, range) {
  return toString_1(charSequenceSubSequence(_this__u8e3s4, range.j7(), range.k7() + 1 | 0));
}
function get_lastIndex_1(_this__u8e3s4) {
  return charSequenceLength(_this__u8e3s4) - 1 | 0;
}
function requireNonNegativeLimit(limit) {
  // Inline function 'kotlin.require' call
  if (!(limit >= 0)) {
    var message = 'Limit must be non-negative, but was ' + limit;
    throw IllegalArgumentException_init_$Create$_0(toString_1(message));
  }
  return Unit_instance;
}
function indexOf_2(_this__u8e3s4, string, startIndex, ignoreCase) {
  startIndex = startIndex === VOID ? 0 : startIndex;
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  var tmp;
  var tmp_0;
  if (ignoreCase) {
    tmp_0 = true;
  } else {
    tmp_0 = !(typeof _this__u8e3s4 === 'string');
  }
  if (tmp_0) {
    tmp = indexOf_3(_this__u8e3s4, string, startIndex, charSequenceLength(_this__u8e3s4), ignoreCase);
  } else {
    // Inline function 'kotlin.text.nativeIndexOf' call
    // Inline function 'kotlin.js.asDynamic' call
    tmp = _this__u8e3s4.indexOf(string, startIndex);
  }
  return tmp;
}
function calcNext_0($this) {
  if ($this.da_1 < 0) {
    $this.ba_1 = 0;
    $this.ea_1 = null;
  } else {
    var tmp;
    var tmp_0;
    if ($this.ga_1.ja_1 > 0) {
      $this.fa_1 = $this.fa_1 + 1 | 0;
      tmp_0 = $this.fa_1 >= $this.ga_1.ja_1;
    } else {
      tmp_0 = false;
    }
    if (tmp_0) {
      tmp = true;
    } else {
      tmp = $this.da_1 > charSequenceLength($this.ga_1.ha_1);
    }
    if (tmp) {
      $this.ea_1 = numberRangeToNumber($this.ca_1, get_lastIndex_1($this.ga_1.ha_1));
      $this.da_1 = -1;
    } else {
      var match = $this.ga_1.ka_1($this.ga_1.ha_1, $this.da_1);
      if (match == null) {
        $this.ea_1 = numberRangeToNumber($this.ca_1, get_lastIndex_1($this.ga_1.ha_1));
        $this.da_1 = -1;
      } else {
        var index = match.y8();
        var length = match.z8();
        $this.ea_1 = until($this.ca_1, index);
        $this.ca_1 = index + length | 0;
        $this.da_1 = $this.ca_1 + (length === 0 ? 1 : 0) | 0;
      }
    }
    $this.ba_1 = 1;
  }
}
function DelimitedRangesSequence$iterator$1(this$0) {
  this.ga_1 = this$0;
  this.ba_1 = -1;
  this.ca_1 = coerceIn(this$0.ia_1, 0, charSequenceLength(this$0.ha_1));
  this.da_1 = this.ca_1;
  this.ea_1 = null;
  this.fa_1 = 0;
}
protoOf(DelimitedRangesSequence$iterator$1).i = function () {
  if (this.ba_1 === -1) {
    calcNext_0(this);
  }
  if (this.ba_1 === 0)
    throw NoSuchElementException_init_$Create$();
  var tmp = this.ea_1;
  var result = tmp instanceof IntRange ? tmp : THROW_CCE();
  this.ea_1 = null;
  this.ba_1 = -1;
  return result;
};
protoOf(DelimitedRangesSequence$iterator$1).h = function () {
  if (this.ba_1 === -1) {
    calcNext_0(this);
  }
  return this.ba_1 === 1;
};
function DelimitedRangesSequence(input, startIndex, limit, getNextMatch) {
  this.ha_1 = input;
  this.ia_1 = startIndex;
  this.ja_1 = limit;
  this.ka_1 = getNextMatch;
}
protoOf(DelimitedRangesSequence).g = function () {
  return new DelimitedRangesSequence$iterator$1(this);
};
function indexOf_3(_this__u8e3s4, other, startIndex, endIndex, ignoreCase, last) {
  last = last === VOID ? false : last;
  var indices = !last ? numberRangeToNumber(coerceAtLeast(startIndex, 0), coerceAtMost(endIndex, charSequenceLength(_this__u8e3s4))) : downTo(coerceAtMost(startIndex, get_lastIndex_1(_this__u8e3s4)), coerceAtLeast(endIndex, 0));
  var tmp;
  if (typeof _this__u8e3s4 === 'string') {
    tmp = typeof other === 'string';
  } else {
    tmp = false;
  }
  if (tmp) {
    var inductionVariable = indices.u9_1;
    var last_0 = indices.v9_1;
    var step = indices.w9_1;
    if (step > 0 && inductionVariable <= last_0 || (step < 0 && last_0 <= inductionVariable))
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + step | 0;
        if (regionMatches(other, 0, _this__u8e3s4, index, other.length, ignoreCase))
          return index;
      }
       while (!(index === last_0));
  } else {
    var inductionVariable_0 = indices.u9_1;
    var last_1 = indices.v9_1;
    var step_0 = indices.w9_1;
    if (step_0 > 0 && inductionVariable_0 <= last_1 || (step_0 < 0 && last_1 <= inductionVariable_0))
      do {
        var index_0 = inductionVariable_0;
        inductionVariable_0 = inductionVariable_0 + step_0 | 0;
        if (regionMatchesImpl(other, 0, _this__u8e3s4, index_0, charSequenceLength(other), ignoreCase))
          return index_0;
      }
       while (!(index_0 === last_1));
  }
  return -1;
}
function regionMatchesImpl(_this__u8e3s4, thisOffset, other, otherOffset, length, ignoreCase) {
  if (otherOffset < 0 || thisOffset < 0 || thisOffset > (charSequenceLength(_this__u8e3s4) - length | 0) || otherOffset > (charSequenceLength(other) - length | 0)) {
    return false;
  }
  var inductionVariable = 0;
  if (inductionVariable < length)
    do {
      var index = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      if (!equals_0(charSequenceGet(_this__u8e3s4, thisOffset + index | 0), charSequenceGet(other, otherOffset + index | 0), ignoreCase))
        return false;
    }
     while (inductionVariable < length);
  return true;
}
function startsWith_0(_this__u8e3s4, char, ignoreCase) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  return charSequenceLength(_this__u8e3s4) > 0 && equals_0(charSequenceGet(_this__u8e3s4, 0), char, ignoreCase);
}
function endsWith(_this__u8e3s4, char, ignoreCase) {
  ignoreCase = ignoreCase === VOID ? false : ignoreCase;
  return charSequenceLength(_this__u8e3s4) > 0 && equals_0(charSequenceGet(_this__u8e3s4, get_lastIndex_1(_this__u8e3s4)), char, ignoreCase);
}
function trimEnd(_this__u8e3s4, chars) {
  // Inline function 'kotlin.text.trimEnd' call
  var tmp0 = isCharSequence(_this__u8e3s4) ? _this__u8e3s4 : THROW_CCE();
  var tmp$ret$1;
  $l$block: {
    // Inline function 'kotlin.text.trimEnd' call
    var inductionVariable = charSequenceLength(tmp0) - 1 | 0;
    if (0 <= inductionVariable)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + -1 | 0;
        var it = charSequenceGet(tmp0, index);
        if (!contains_0(chars, it)) {
          tmp$ret$1 = charSequenceSubSequence(tmp0, 0, index + 1 | 0);
          break $l$block;
        }
      }
       while (0 <= inductionVariable);
    tmp$ret$1 = '';
  }
  return toString_1(tmp$ret$1);
}
function trimStart(_this__u8e3s4, chars) {
  // Inline function 'kotlin.text.trimStart' call
  var tmp0 = isCharSequence(_this__u8e3s4) ? _this__u8e3s4 : THROW_CCE();
  var tmp$ret$1;
  $l$block: {
    // Inline function 'kotlin.text.trimStart' call
    var inductionVariable = 0;
    var last = charSequenceLength(tmp0) - 1 | 0;
    if (inductionVariable <= last)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var it = charSequenceGet(tmp0, index);
        if (!contains_0(chars, it)) {
          tmp$ret$1 = charSequenceSubSequence(tmp0, index, charSequenceLength(tmp0));
          break $l$block;
        }
      }
       while (inductionVariable <= last);
    tmp$ret$1 = '';
  }
  return toString_1(tmp$ret$1);
}
function rangesDelimitedBy$lambda($delimiters, $ignoreCase) {
  return function ($this$DelimitedRangesSequence, currentIndex) {
    // Inline function 'kotlin.let' call
    var it = indexOfAny($this$DelimitedRangesSequence, $delimiters, currentIndex, $ignoreCase);
    return it < 0 ? null : to(it, 1);
  };
}
function MatchNamedGroupCollection() {
}
function UnsafeLazyImpl(initializer) {
  this.la_1 = initializer;
  this.ma_1 = UNINITIALIZED_VALUE_instance;
}
protoOf(UnsafeLazyImpl).k1 = function () {
  if (this.ma_1 === UNINITIALIZED_VALUE_instance) {
    this.ma_1 = ensureNotNull(this.la_1)();
    this.la_1 = null;
  }
  return this.ma_1;
};
protoOf(UnsafeLazyImpl).na = function () {
  return !(this.ma_1 === UNINITIALIZED_VALUE_instance);
};
protoOf(UnsafeLazyImpl).toString = function () {
  return this.na() ? toString_0(this.k1()) : 'Lazy value not initialized yet.';
};
function UNINITIALIZED_VALUE() {
}
var UNINITIALIZED_VALUE_instance;
function UNINITIALIZED_VALUE_getInstance() {
  return UNINITIALIZED_VALUE_instance;
}
function Pair(first, second) {
  this.w8_1 = first;
  this.x8_1 = second;
}
protoOf(Pair).toString = function () {
  return '(' + toString_0(this.w8_1) + ', ' + toString_0(this.x8_1) + ')';
};
protoOf(Pair).y8 = function () {
  return this.w8_1;
};
protoOf(Pair).z8 = function () {
  return this.x8_1;
};
protoOf(Pair).hashCode = function () {
  var result = this.w8_1 == null ? 0 : hashCode_0(this.w8_1);
  result = imul_0(result, 31) + (this.x8_1 == null ? 0 : hashCode_0(this.x8_1)) | 0;
  return result;
};
protoOf(Pair).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Pair))
    return false;
  if (!equals(this.w8_1, other.w8_1))
    return false;
  if (!equals(this.x8_1, other.x8_1))
    return false;
  return true;
};
function to(_this__u8e3s4, that) {
  return new Pair(_this__u8e3s4, that);
}
function Span(start, end) {
  this.oa_1 = start;
  this.pa_1 = end;
}
protoOf(Span).toString = function () {
  return 'Span(start=' + this.oa_1 + ', end=' + this.pa_1 + ')';
};
protoOf(Span).hashCode = function () {
  var result = this.oa_1;
  result = imul_0(result, 31) + this.pa_1 | 0;
  return result;
};
protoOf(Span).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Span))
    return false;
  if (!(this.oa_1 === other.oa_1))
    return false;
  if (!(this.pa_1 === other.pa_1))
    return false;
  return true;
};
var static_init_called;
function static_init() {
  if (static_init_called)
    return Unit_instance;
  static_init_called = true;
  Op_EQ_instance = new Op('EQ', 0, ':');
  Op_EXACT_instance = new Op('EXACT', 1, '=');
  Op_NEQ_instance = new Op('NEQ', 2, '!=');
  Op_LE_instance = new Op('LE', 3, '<=');
  Op_GE_instance = new Op('GE', 4, '>=');
  Op_LT_instance = new Op('LT', 5, '<');
  Op_GT_instance = new Op('GT', 6, '>');
  if (Companion_instance_11 == null) {
    Companion_instance_0;
    new Companion_11();
  }
}
var Op_EQ_instance;
var Op_EXACT_instance;
var Op_NEQ_instance;
var Op_LE_instance;
var Op_GE_instance;
var Op_LT_instance;
var Op_GT_instance;
function Companion_11() {
  Companion_instance_11 = this;
  this.qa_1 = listOf_0([Op_LE_getInstance(), Op_GE_getInstance(), Op_NEQ_getInstance(), Op_EQ_getInstance(), Op_EXACT_getInstance(), Op_LT_getInstance(), Op_GT_getInstance()]);
}
var Companion_instance_11;
function Companion_getInstance_11() {
  static_init();
  return Companion_instance_11;
}
function Op(name, ordinal, text) {
  Enum.call(this, name, ordinal);
  this.ta_1 = text;
}
function LParen() {
}
var LParen_instance;
function LParen_getInstance() {
  return LParen_instance;
}
function RParen() {
}
var RParen_instance;
function RParen_getInstance() {
  return RParen_instance;
}
function Or() {
}
var Or_instance;
function Or_getInstance() {
  return Or_instance;
}
function And() {
}
var And_instance;
function And_getInstance() {
  return And_instance;
}
function Not() {
}
var Not_instance;
function Not_getInstance() {
  return Not_instance;
}
function Term() {
}
var Term_instance;
function Term_getInstance() {
  return Term_instance;
}
function Token(kind, span, negate, key, op, value, regex, regexFlags, exact, keySpan) {
  negate = negate === VOID ? false : negate;
  key = key === VOID ? null : key;
  op = op === VOID ? Op_EQ_getInstance() : op;
  value = value === VOID ? '' : value;
  regex = regex === VOID ? false : regex;
  regexFlags = regexFlags === VOID ? '' : regexFlags;
  exact = exact === VOID ? false : exact;
  keySpan = keySpan === VOID ? null : keySpan;
  this.ua_1 = kind;
  this.va_1 = span;
  this.wa_1 = negate;
  this.xa_1 = key;
  this.ya_1 = op;
  this.za_1 = value;
  this.ab_1 = regex;
  this.bb_1 = regexFlags;
  this.cb_1 = exact;
  this.db_1 = keySpan;
}
protoOf(Token).toString = function () {
  return 'Token(kind=' + toString_1(this.ua_1) + ', span=' + this.va_1.toString() + ', negate=' + this.wa_1 + ', key=' + this.xa_1 + ', op=' + this.ya_1.toString() + ', value=' + this.za_1 + ', regex=' + this.ab_1 + ', regexFlags=' + this.bb_1 + ', exact=' + this.cb_1 + ', keySpan=' + toString_0(this.db_1) + ')';
};
protoOf(Token).hashCode = function () {
  var result = hashCode_0(this.ua_1);
  result = imul_0(result, 31) + this.va_1.hashCode() | 0;
  result = imul_0(result, 31) + getBooleanHashCode(this.wa_1) | 0;
  result = imul_0(result, 31) + (this.xa_1 == null ? 0 : getStringHashCode(this.xa_1)) | 0;
  result = imul_0(result, 31) + this.ya_1.hashCode() | 0;
  result = imul_0(result, 31) + getStringHashCode(this.za_1) | 0;
  result = imul_0(result, 31) + getBooleanHashCode(this.ab_1) | 0;
  result = imul_0(result, 31) + getStringHashCode(this.bb_1) | 0;
  result = imul_0(result, 31) + getBooleanHashCode(this.cb_1) | 0;
  result = imul_0(result, 31) + (this.db_1 == null ? 0 : this.db_1.hashCode()) | 0;
  return result;
};
protoOf(Token).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Token))
    return false;
  if (!equals(this.ua_1, other.ua_1))
    return false;
  if (!this.va_1.equals(other.va_1))
    return false;
  if (!(this.wa_1 === other.wa_1))
    return false;
  if (!(this.xa_1 == other.xa_1))
    return false;
  if (!this.ya_1.equals(other.ya_1))
    return false;
  if (!(this.za_1 === other.za_1))
    return false;
  if (!(this.ab_1 === other.ab_1))
    return false;
  if (!(this.bb_1 === other.bb_1))
    return false;
  if (!(this.cb_1 === other.cb_1))
    return false;
  if (!equals(this.db_1, other.db_1))
    return false;
  return true;
};
function AndNode(children, span) {
  this.eb_1 = children;
  this.fb_1 = span;
}
protoOf(AndNode).gb = function () {
  return this.fb_1;
};
protoOf(AndNode).hb = function (children, span) {
  return new AndNode(children, span);
};
protoOf(AndNode).ib = function (children, span, $super) {
  children = children === VOID ? this.eb_1 : children;
  span = span === VOID ? this.fb_1 : span;
  return $super === VOID ? this.hb(children, span) : $super.hb.call(this, children, span);
};
protoOf(AndNode).toString = function () {
  return 'AndNode(children=' + toString_1(this.eb_1) + ', span=' + this.fb_1.toString() + ')';
};
protoOf(AndNode).hashCode = function () {
  var result = hashCode_0(this.eb_1);
  result = imul_0(result, 31) + this.fb_1.hashCode() | 0;
  return result;
};
protoOf(AndNode).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof AndNode))
    return false;
  if (!equals(this.eb_1, other.eb_1))
    return false;
  if (!this.fb_1.equals(other.fb_1))
    return false;
  return true;
};
function OrNode(children, span) {
  this.jb_1 = children;
  this.kb_1 = span;
}
protoOf(OrNode).gb = function () {
  return this.kb_1;
};
protoOf(OrNode).hb = function (children, span) {
  return new OrNode(children, span);
};
protoOf(OrNode).lb = function (children, span, $super) {
  children = children === VOID ? this.jb_1 : children;
  span = span === VOID ? this.kb_1 : span;
  return $super === VOID ? this.hb(children, span) : $super.hb.call(this, children, span);
};
protoOf(OrNode).toString = function () {
  return 'OrNode(children=' + toString_1(this.jb_1) + ', span=' + this.kb_1.toString() + ')';
};
protoOf(OrNode).hashCode = function () {
  var result = hashCode_0(this.jb_1);
  result = imul_0(result, 31) + this.kb_1.hashCode() | 0;
  return result;
};
protoOf(OrNode).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof OrNode))
    return false;
  if (!equals(this.jb_1, other.jb_1))
    return false;
  if (!this.kb_1.equals(other.kb_1))
    return false;
  return true;
};
function NotNode(child, span) {
  this.mb_1 = child;
  this.nb_1 = span;
}
protoOf(NotNode).gb = function () {
  return this.nb_1;
};
protoOf(NotNode).ob = function (child, span) {
  return new NotNode(child, span);
};
protoOf(NotNode).pb = function (child, span, $super) {
  child = child === VOID ? this.mb_1 : child;
  span = span === VOID ? this.nb_1 : span;
  return $super === VOID ? this.ob(child, span) : $super.ob.call(this, child, span);
};
protoOf(NotNode).toString = function () {
  return 'NotNode(child=' + toString_1(this.mb_1) + ', span=' + this.nb_1.toString() + ')';
};
protoOf(NotNode).hashCode = function () {
  var result = hashCode_0(this.mb_1);
  result = imul_0(result, 31) + this.nb_1.hashCode() | 0;
  return result;
};
protoOf(NotNode).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof NotNode))
    return false;
  if (!equals(this.mb_1, other.mb_1))
    return false;
  if (!this.nb_1.equals(other.nb_1))
    return false;
  return true;
};
function AtomNode(key, op, value, regex, regexFlags, exact, span, keySpan) {
  keySpan = keySpan === VOID ? null : keySpan;
  this.qb_1 = key;
  this.rb_1 = op;
  this.sb_1 = value;
  this.tb_1 = regex;
  this.ub_1 = regexFlags;
  this.vb_1 = exact;
  this.wb_1 = span;
  this.xb_1 = keySpan;
}
protoOf(AtomNode).gb = function () {
  return this.wb_1;
};
protoOf(AtomNode).yb = function (key, op, value, regex, regexFlags, exact, span, keySpan) {
  return new AtomNode(key, op, value, regex, regexFlags, exact, span, keySpan);
};
protoOf(AtomNode).zb = function (key, op, value, regex, regexFlags, exact, span, keySpan, $super) {
  key = key === VOID ? this.qb_1 : key;
  op = op === VOID ? this.rb_1 : op;
  value = value === VOID ? this.sb_1 : value;
  regex = regex === VOID ? this.tb_1 : regex;
  regexFlags = regexFlags === VOID ? this.ub_1 : regexFlags;
  exact = exact === VOID ? this.vb_1 : exact;
  span = span === VOID ? this.wb_1 : span;
  keySpan = keySpan === VOID ? this.xb_1 : keySpan;
  return $super === VOID ? this.yb(key, op, value, regex, regexFlags, exact, span, keySpan) : $super.yb.call(this, key, op, value, regex, regexFlags, exact, span, keySpan);
};
protoOf(AtomNode).toString = function () {
  return 'AtomNode(key=' + this.qb_1 + ', op=' + this.rb_1.toString() + ', value=' + this.sb_1 + ', regex=' + this.tb_1 + ', regexFlags=' + this.ub_1 + ', exact=' + this.vb_1 + ', span=' + this.wb_1.toString() + ', keySpan=' + toString_0(this.xb_1) + ')';
};
protoOf(AtomNode).hashCode = function () {
  var result = this.qb_1 == null ? 0 : getStringHashCode(this.qb_1);
  result = imul_0(result, 31) + this.rb_1.hashCode() | 0;
  result = imul_0(result, 31) + getStringHashCode(this.sb_1) | 0;
  result = imul_0(result, 31) + getBooleanHashCode(this.tb_1) | 0;
  result = imul_0(result, 31) + getStringHashCode(this.ub_1) | 0;
  result = imul_0(result, 31) + getBooleanHashCode(this.vb_1) | 0;
  result = imul_0(result, 31) + this.wb_1.hashCode() | 0;
  result = imul_0(result, 31) + (this.xb_1 == null ? 0 : this.xb_1.hashCode()) | 0;
  return result;
};
protoOf(AtomNode).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof AtomNode))
    return false;
  if (!(this.qb_1 == other.qb_1))
    return false;
  if (!this.rb_1.equals(other.rb_1))
    return false;
  if (!(this.sb_1 === other.sb_1))
    return false;
  if (!(this.tb_1 === other.tb_1))
    return false;
  if (!(this.ub_1 === other.ub_1))
    return false;
  if (!(this.vb_1 === other.vb_1))
    return false;
  if (!this.wb_1.equals(other.wb_1))
    return false;
  if (!equals(this.xb_1, other.xb_1))
    return false;
  return true;
};
function ParseError(message, span, suggestion, fix) {
  suggestion = suggestion === VOID ? null : suggestion;
  fix = fix === VOID ? null : fix;
  this.ac_1 = message;
  this.bc_1 = span;
  this.cc_1 = suggestion;
  this.dc_1 = fix;
}
protoOf(ParseError).toString = function () {
  return 'ParseError(message=' + this.ac_1 + ', span=' + this.bc_1.toString() + ', suggestion=' + this.cc_1 + ', fix=' + toString_0(this.dc_1) + ')';
};
protoOf(ParseError).hashCode = function () {
  var result = getStringHashCode(this.ac_1);
  result = imul_0(result, 31) + this.bc_1.hashCode() | 0;
  result = imul_0(result, 31) + (this.cc_1 == null ? 0 : getStringHashCode(this.cc_1)) | 0;
  result = imul_0(result, 31) + (this.dc_1 == null ? 0 : this.dc_1.hashCode()) | 0;
  return result;
};
protoOf(ParseError).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof ParseError))
    return false;
  if (!(this.ac_1 === other.ac_1))
    return false;
  if (!this.bc_1.equals(other.bc_1))
    return false;
  if (!(this.cc_1 == other.cc_1))
    return false;
  if (!equals(this.dc_1, other.dc_1))
    return false;
  return true;
};
function Fix(span, text) {
  this.ec_1 = span;
  this.fc_1 = text;
}
protoOf(Fix).toString = function () {
  return 'Fix(span=' + this.ec_1.toString() + ', text=' + this.fc_1 + ')';
};
protoOf(Fix).hashCode = function () {
  var result = this.ec_1.hashCode();
  result = imul_0(result, 31) + getStringHashCode(this.fc_1) | 0;
  return result;
};
protoOf(Fix).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Fix))
    return false;
  if (!this.ec_1.equals(other.ec_1))
    return false;
  if (!(this.fc_1 === other.fc_1))
    return false;
  return true;
};
function Op_EQ_getInstance() {
  static_init();
  return Op_EQ_instance;
}
function Op_EXACT_getInstance() {
  static_init();
  return Op_EXACT_instance;
}
function Op_NEQ_getInstance() {
  static_init();
  return Op_NEQ_instance;
}
function Op_LE_getInstance() {
  static_init();
  return Op_LE_instance;
}
function Op_GE_getInstance() {
  static_init();
  return Op_GE_instance;
}
function Op_LT_getInstance() {
  static_init();
  return Op_LT_instance;
}
function Op_GT_getInstance() {
  static_init();
  return Op_GT_instance;
}
function Colorless() {
}
var Colorless_instance;
function Colorless_getInstance() {
  return Colorless_instance;
}
function Multi() {
}
var Multi_instance;
function Multi_getInstance() {
  return Multi_instance;
}
function Mono() {
}
var Mono_instance;
function Mono_getInstance() {
  return Mono_instance;
}
function Colors(set) {
  this.gc_1 = set;
}
protoOf(Colors).toString = function () {
  return 'Colors(set=' + toString_1(this.gc_1) + ')';
};
protoOf(Colors).hashCode = function () {
  return hashCode_0(this.gc_1);
};
protoOf(Colors).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Colors))
    return false;
  if (!equals(this.gc_1, other.gc_1))
    return false;
  return true;
};
function Count(value) {
  this.hc_1 = value;
}
protoOf(Count).toString = function () {
  return 'Count(value=' + this.hc_1 + ')';
};
protoOf(Count).hashCode = function () {
  return this.hc_1;
};
protoOf(Count).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Count))
    return false;
  if (!(this.hc_1 === other.hc_1))
    return false;
  return true;
};
function Error_0(message) {
  this.ic_1 = message;
}
protoOf(Error_0).toString = function () {
  return 'Error(message=' + this.ic_1 + ')';
};
protoOf(Error_0).hashCode = function () {
  return getStringHashCode(this.ic_1);
};
protoOf(Error_0).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Error_0))
    return false;
  if (!(this.ic_1 === other.ic_1))
    return false;
  return true;
};
function ColorSugar() {
  ColorSugar_instance = this;
  this.jc_1 = mapOf([to(new Char(_Char___init__impl__6a9atx(119)), 'WHITE'), to(new Char(_Char___init__impl__6a9atx(117)), 'BLUE'), to(new Char(_Char___init__impl__6a9atx(98)), 'BLACK'), to(new Char(_Char___init__impl__6a9atx(114)), 'RED'), to(new Char(_Char___init__impl__6a9atx(103)), 'GREEN')]);
  this.kc_1 = mapOf([to('white', 'WHITE'), to('blue', 'BLUE'), to('black', 'BLACK'), to('red', 'RED'), to('green', 'GREEN')]);
  this.lc_1 = mapOf([to('azorius', listOf_0(['WHITE', 'BLUE'])), to('dimir', listOf_0(['BLUE', 'BLACK'])), to('rakdos', listOf_0(['BLACK', 'RED'])), to('gruul', listOf_0(['RED', 'GREEN'])), to('selesnya', listOf_0(['GREEN', 'WHITE'])), to('orzhov', listOf_0(['WHITE', 'BLACK'])), to('izzet', listOf_0(['BLUE', 'RED'])), to('golgari', listOf_0(['BLACK', 'GREEN'])), to('boros', listOf_0(['RED', 'WHITE'])), to('simic', listOf_0(['GREEN', 'BLUE'])), to('bant', listOf_0(['GREEN', 'WHITE', 'BLUE'])), to('esper', listOf_0(['WHITE', 'BLUE', 'BLACK'])), to('grixis', listOf_0(['BLUE', 'BLACK', 'RED'])), to('jund', listOf_0(['BLACK', 'RED', 'GREEN'])), to('naya', listOf_0(['RED', 'GREEN', 'WHITE'])), to('abzan', listOf_0(['WHITE', 'BLACK', 'GREEN'])), to('jeskai', listOf_0(['BLUE', 'RED', 'WHITE'])), to('sultai', listOf_0(['BLACK', 'GREEN', 'BLUE'])), to('mardu', listOf_0(['RED', 'WHITE', 'BLACK'])), to('temur', listOf_0(['GREEN', 'BLUE', 'RED'])), to('yore-tiller', listOf_0(['WHITE', 'BLUE', 'BLACK', 'RED'])), to('glint-eye', listOf_0(['BLUE', 'BLACK', 'RED', 'GREEN'])), to('dune-brood', listOf_0(['BLACK', 'RED', 'GREEN', 'WHITE'])), to('ink-treader', listOf_0(['RED', 'GREEN', 'WHITE', 'BLUE'])), to('witch-maw', listOf_0(['GREEN', 'WHITE', 'BLUE', 'BLACK'])), to('wubrg', listOf_0(['WHITE', 'BLUE', 'BLACK', 'RED', 'GREEN'])), to('fivecolor', listOf_0(['WHITE', 'BLUE', 'BLACK', 'RED', 'GREEN']))]);
}
protoOf(ColorSugar).mc = function (raw) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  // Inline function 'kotlin.text.trim' call
  var this_0 = raw.toLowerCase();
  var v = toString_1(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(v) === 0)
    return new Error_0('Empty color value.');
  var tmp0_safe_receiver = toIntOrNull(v);
  if (tmp0_safe_receiver == null)
    null;
  else {
    // Inline function 'kotlin.let' call
    return new Count(tmp0_safe_receiver);
  }
  if (v === 'c' || v === 'colorless')
    return Colorless_instance;
  switch (v) {
    case 'm':
    case 'multi':
    case 'multicolor':
    case 'multicolour':
      return Multi_instance;
  }
  switch (v) {
    case 'mono':
    case 'monocolor':
    case 'monocolour':
      return Mono_instance;
  }
  var tmp1_safe_receiver = this.kc_1.n1(v);
  if (tmp1_safe_receiver == null)
    null;
  else {
    // Inline function 'kotlin.let' call
    return new Colors(setOf(tmp1_safe_receiver));
  }
  var tmp2_safe_receiver = this.lc_1.n1(v);
  if (tmp2_safe_receiver == null)
    null;
  else {
    // Inline function 'kotlin.let' call
    return new Colors(toSet(tmp2_safe_receiver));
  }
  var set = HashSet_init_$Create$();
  var inductionVariable = 0;
  var last = v.length;
  while (inductionVariable < last) {
    var ch = charCodeAt(v, inductionVariable);
    inductionVariable = inductionVariable + 1 | 0;
    var tmp3_elvis_lhs = this.jc_1.n1(new Char(ch));
    var tmp;
    if (tmp3_elvis_lhs == null) {
      return new Error_0('Unknown color "' + toString(ch) + '". Use w/u/b/r/g, a guild/shard/wedge name, or a number.');
    } else {
      tmp = tmp3_elvis_lhs;
    }
    var color = tmp;
    // Inline function 'kotlin.collections.plusAssign' call
    set.f(color);
  }
  return new Colors(set);
};
var ColorSugar_instance;
function ColorSugar_getInstance() {
  if (ColorSugar_instance == null)
    new ColorSugar();
  return ColorSugar_instance;
}
function CompileResult(predicate, errors) {
  this.nc_1 = predicate;
  this.oc_1 = errors;
}
protoOf(CompileResult).toString = function () {
  return 'CompileResult(predicate=' + toString_1(this.nc_1) + ', errors=' + toString_1(this.oc_1) + ')';
};
protoOf(CompileResult).hashCode = function () {
  var result = hashCode_0(this.nc_1);
  result = imul_0(result, 31) + hashCode_0(this.oc_1) | 0;
  return result;
};
protoOf(CompileResult).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof CompileResult))
    return false;
  if (!equals(this.nc_1, other.nc_1))
    return false;
  if (!equals(this.oc_1, other.oc_1))
    return false;
  return true;
};
function walk($this, node, errors) {
  var tmp;
  if (node instanceof AndNode) {
    // Inline function 'kotlin.collections.map' call
    var this_0 = node.eb_1;
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
    var _iterator__ex2g4s = this_0.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      var tmp$ret$2 = walk(Evaluator_getInstance(), item, errors);
      destination.f(tmp$ret$2);
    }
    tmp = andOf($this, destination);
  } else {
    if (node instanceof OrNode) {
      // Inline function 'kotlin.collections.map' call
      var this_1 = node.jb_1;
      // Inline function 'kotlin.collections.mapTo' call
      var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_1, 10));
      var _iterator__ex2g4s_0 = this_1.g();
      while (_iterator__ex2g4s_0.h()) {
        var item_0 = _iterator__ex2g4s_0.i();
        var tmp$ret$5 = walk(Evaluator_getInstance(), item_0, errors);
        destination_0.f(tmp$ret$5);
      }
      tmp = orOf($this, destination_0);
    } else {
      if (node instanceof NotNode) {
        tmp = notOf($this, walk($this, node.mb_1, errors));
      } else {
        if (node instanceof AtomNode) {
          tmp = compileAtom($this, node, errors);
        } else {
          noWhenBranchMatchedException();
        }
      }
    }
  }
  return tmp;
}
function andOf($this, ps) {
  // Inline function 'kotlin.collections.toTypedArray' call
  var all = copyToArray(ps);
  return Evaluator$andOf$lambda(all);
}
function orOf($this, ps) {
  // Inline function 'kotlin.collections.toTypedArray' call
  var any = copyToArray(ps);
  return Evaluator$orOf$lambda(any);
}
function notOf($this, p) {
  return Evaluator$notOf$lambda(p);
}
function compileAtom($this, atom, errors) {
  if (atom.qb_1 == null) {
    var matcher = ensureNotNull(Matchers_getInstance().md_1.n1('name'));
    return runMatcher($this, matcher, atom, errors);
  }
  var matcher_0 = Matchers_getInstance().md_1.n1(atom.qb_1);
  if (matcher_0 == null) {
    var sugg = Matchers_getInstance().od(atom.qb_1);
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new ParseError('Unknown filter "' + atom.qb_1 + '".', atom.wb_1, !(sugg == null) ? 'Did you mean "' + sugg + ':"?' : null, !(sugg == null) && !(atom.xb_1 == null) ? new Fix(atom.xb_1, sugg) : null);
    errors.f(element);
    return $this.qd_1;
  }
  if (!matcher_0.rd().h1(atom.rb_1)) {
    var tmp = 'Operator "' + atom.rb_1.ta_1 + '" is not supported on "' + atom.qb_1 + '". ';
    var tmp_0 = matcher_0.rd();
    // Inline function 'kotlin.collections.plusAssign' call
    var element_0 = new ParseError(tmp + ('Try ' + joinToString_0(tmp_0, ', ', VOID, VOID, VOID, VOID, Evaluator$compileAtom$lambda) + '.'), atom.wb_1);
    errors.f(element_0);
    return $this.qd_1;
  }
  return runMatcher($this, matcher_0, atom, errors);
}
function runMatcher($this, matcher, atom, errors) {
  var r = matcher.sd(atom);
  var tmp;
  if (r instanceof Ok) {
    tmp = r.vd_1;
  } else {
    if (r instanceof Err) {
      // Inline function 'kotlin.collections.plusAssign' call
      var element = new ParseError(r.td_1, atom.wb_1, r.ud_1);
      errors.f(element);
      tmp = $this.qd_1;
    } else {
      noWhenBranchMatchedException();
    }
  }
  return tmp;
}
function Evaluator$TRUE$lambda(it) {
  return true;
}
function Evaluator$FALSE$lambda(it) {
  return false;
}
function Evaluator$andOf$lambda($all) {
  return function (c) {
    var ok = true;
    var indexedObject = $all;
    var inductionVariable = 0;
    var last = indexedObject.length;
    $l$loop: while (inductionVariable < last) {
      var p = indexedObject[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      if (!p(c)) {
        ok = false;
        break $l$loop;
      }
    }
    return ok;
  };
}
function Evaluator$orOf$lambda($any) {
  return function (c) {
    var ok = false;
    var indexedObject = $any;
    var inductionVariable = 0;
    var last = indexedObject.length;
    $l$loop: while (inductionVariable < last) {
      var p = indexedObject[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      if (p(c)) {
        ok = true;
        break $l$loop;
      }
    }
    return ok;
  };
}
function Evaluator$notOf$lambda($p) {
  return function (c) {
    return !$p(c);
  };
}
function Evaluator$compileAtom$lambda(it) {
  return '"' + it.ta_1 + '"';
}
function Evaluator() {
  Evaluator_instance = this;
  var tmp = this;
  tmp.pd_1 = Evaluator$TRUE$lambda;
  var tmp_0 = this;
  tmp_0.qd_1 = Evaluator$FALSE$lambda;
}
protoOf(Evaluator).wd = function (ast) {
  var errors = ArrayList_init_$Create$();
  if (ast == null)
    return new CompileResult(this.pd_1, errors);
  var predicate = walk(this, ast, errors);
  return new CompileResult(predicate, errors);
};
var Evaluator_instance;
function Evaluator_getInstance() {
  if (Evaluator_instance == null)
    new Evaluator();
  return Evaluator_instance;
}
function buildTerm($this, raw, span) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var lc = raw.toLowerCase();
  var tmp;
  var tmp_0;
  // Inline function 'kotlin.text.isNotEmpty' call
  if (charSequenceLength(raw) > 0) {
    tmp_0 = !(charCodeAt(raw, 0) === _Char___init__impl__6a9atx(45));
  } else {
    tmp_0 = false;
  }
  if (tmp_0) {
    tmp = !(charCodeAt(raw, 0) === _Char___init__impl__6a9atx(33));
  } else {
    tmp = false;
  }
  if (tmp) {
    switch (lc) {
      case 'and':
        return new Token(And_instance, span);
      case 'or':
        return new Token(Or_instance, span);
      case 'not':
        return new Token(Not_instance, span);
    }
  }
  var term = raw;
  var negate = false;
  var exact = false;
  if (startsWith(term, '-') && term.length > 1) {
    negate = true;
    term = substring_0(term, 1);
  }
  if (startsWith(term, '!') && term.length > 1) {
    exact = true;
    term = substring_0(term, 1);
  }
  var match = findKeyOp($this, term);
  if (!(match == null)) {
    var key = match.y8();
    var op = match.z8();
    var valueStart = match.ae();
    var keyStart = (span.oa_1 + (negate ? 1 : 0) | 0) + (exact ? 1 : 0) | 0;
    var parsed = parseValue($this, substring_0(term, valueStart), true);
    var tmp_1 = Term_instance;
    var tmp_2 = negate;
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp$ret$3 = key.toLowerCase();
    return new Token(tmp_1, span, tmp_2, tmp$ret$3, op, parsed.be_1, parsed.ce_1, parsed.de_1, exact, new Span(keyStart, keyStart + key.length | 0));
  }
  var parsed_0 = parseValue($this, term, false);
  return new Token(Term_instance, span, negate, null, Op_EQ_getInstance(), parsed_0.be_1, parsed_0.ce_1, parsed_0.de_1, exact);
}
function KeyOpMatch(key, op, valueStart) {
  this.xd_1 = key;
  this.yd_1 = op;
  this.zd_1 = valueStart;
}
protoOf(KeyOpMatch).y8 = function () {
  return this.xd_1;
};
protoOf(KeyOpMatch).z8 = function () {
  return this.yd_1;
};
protoOf(KeyOpMatch).ae = function () {
  return this.zd_1;
};
protoOf(KeyOpMatch).toString = function () {
  return 'KeyOpMatch(key=' + this.xd_1 + ', op=' + this.yd_1.toString() + ', valueStart=' + this.zd_1 + ')';
};
protoOf(KeyOpMatch).hashCode = function () {
  var result = getStringHashCode(this.xd_1);
  result = imul_0(result, 31) + this.yd_1.hashCode() | 0;
  result = imul_0(result, 31) + this.zd_1 | 0;
  return result;
};
protoOf(KeyOpMatch).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof KeyOpMatch))
    return false;
  if (!(this.xd_1 === other.xd_1))
    return false;
  if (!this.yd_1.equals(other.yd_1))
    return false;
  if (!(this.zd_1 === other.zd_1))
    return false;
  return true;
};
function findKeyOp($this, term) {
  var inductionVariable = 0;
  var last = charSequenceLength(term) - 1 | 0;
  if (inductionVariable <= last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      var _iterator__ex2g4s = Companion_getInstance_11().qa_1.g();
      $l$loop_0: while (_iterator__ex2g4s.h()) {
        var op = _iterator__ex2g4s.i();
        if ((i + op.ta_1.length | 0) >= term.length)
          continue $l$loop_0;
        if (!regionMatches(term, i, op.ta_1, 0, op.ta_1.length))
          continue $l$loop_0;
        var key = substring(term, 0, i);
        if ($this.ee_1.y6(key)) {
          return new KeyOpMatch(key, op, i + op.ta_1.length | 0);
        }
      }
    }
     while (inductionVariable <= last);
  return null;
}
function ParsedValue(value, regex, regexFlags) {
  this.be_1 = value;
  this.ce_1 = regex;
  this.de_1 = regexFlags;
}
protoOf(ParsedValue).toString = function () {
  return 'ParsedValue(value=' + this.be_1 + ', regex=' + this.ce_1 + ', regexFlags=' + this.de_1 + ')';
};
protoOf(ParsedValue).hashCode = function () {
  var result = getStringHashCode(this.be_1);
  result = imul_0(result, 31) + getBooleanHashCode(this.ce_1) | 0;
  result = imul_0(result, 31) + getStringHashCode(this.de_1) | 0;
  return result;
};
protoOf(ParsedValue).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof ParsedValue))
    return false;
  if (!(this.be_1 === other.be_1))
    return false;
  if (!(this.ce_1 === other.ce_1))
    return false;
  if (!(this.de_1 === other.de_1))
    return false;
  return true;
};
function parseValue($this, raw, allowRegex) {
  if (raw.length >= 2) {
    var first = charCodeAt(raw, 0);
    if ((first === _Char___init__impl__6a9atx(34) || first === _Char___init__impl__6a9atx(39)) && last_0(raw) === first) {
      return new ParsedValue(substring(raw, 1, raw.length - 1 | 0), false, '');
    }
    if (allowRegex && first === _Char___init__impl__6a9atx(47)) {
      var m = $this.fe_1.c7(raw);
      if (!(m == null)) {
        return new ParsedValue(m.c8().k(1), true, m.c8().k(2));
      }
    }
  }
  return new ParsedValue(raw, false, '');
}
function Lexer() {
  Lexer_instance = this;
  this.ee_1 = Regex_init_$Create$('^[a-zA-Z]+$');
  this.fe_1 = Regex_init_$Create$('^/(.+)/([a-zA-Z]*)$');
}
protoOf(Lexer).ge = function (query) {
  var tokens = ArrayList_init_$Create$();
  var i = 0;
  var n = query.length;
  $l$loop_6: while (i < n) {
    var ch = charCodeAt(query, i);
    if (isWhitespace(ch)) {
      i = i + 1 | 0;
      continue $l$loop_6;
    }
    if (ch === _Char___init__impl__6a9atx(40)) {
      tokens.f(new Token(LParen_instance, new Span(i, i + 1 | 0)));
      i = i + 1 | 0;
      continue $l$loop_6;
    }
    if (ch === _Char___init__impl__6a9atx(41)) {
      tokens.f(new Token(RParen_instance, new Span(i, i + 1 | 0)));
      i = i + 1 | 0;
      continue $l$loop_6;
    }
    var start = i;
    var inQuote = false;
    var quoteChar = null;
    $l$loop_5: while (i < n) {
      var c = charCodeAt(query, i);
      if (inQuote) {
        var tmp = quoteChar;
        if (equals(new Char(c), tmp == null ? null : new Char(tmp))) {
          inQuote = false;
          quoteChar = null;
        }
        i = i + 1 | 0;
        continue $l$loop_5;
      }
      if (c === _Char___init__impl__6a9atx(34) || c === _Char___init__impl__6a9atx(39)) {
        inQuote = true;
        quoteChar = c;
        i = i + 1 | 0;
        continue $l$loop_5;
      }
      if (isWhitespace(c))
        break $l$loop_5;
      if (c === _Char___init__impl__6a9atx(40) || c === _Char___init__impl__6a9atx(41))
        break $l$loop_5;
      i = i + 1 | 0;
    }
    var span = new Span(start, i);
    if (i === start)
      continue $l$loop_6;
    tokens.f(buildTerm(this, substring(query, start, i), span));
  }
  return tokens;
};
var Lexer_instance;
function Lexer_getInstance() {
  if (Lexer_instance == null)
    new Lexer();
  return Lexer_instance;
}
function isPip($this, half) {
  return half.length === 1 && contains_1('WUBRG', charCodeAt(half, 0));
}
function CostColors(pips, requirements) {
  this.he_1 = pips;
  this.ie_1 = requirements;
}
function isSubBag($this, sub, sup) {
  var j = 0;
  var inductionVariable = 0;
  var last = sub.length;
  while (inductionVariable < last) {
    var s = sub[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    while (j < sup.length && compareTo(sup[j], s) < 0) {
      j = j + 1 | 0;
    }
    if (j === sup.length || !(sup[j] === s))
      return false;
    j = j + 1 | 0;
  }
  return true;
}
function ManaCostMatch$predicate$lambda($op, $wanted) {
  return function (have) {
    var tmp;
    switch ($op.r1_1) {
      case 0:
      case 1:
        tmp = contentEquals(have, $wanted);
        break;
      case 2:
        tmp = !contentEquals(have, $wanted);
        break;
      case 4:
        tmp = isSubBag(ManaCostMatch_instance, $wanted, have);
        break;
      case 3:
        tmp = isSubBag(ManaCostMatch_instance, have, $wanted);
        break;
      case 6:
        tmp = (have.length > $wanted.length && isSubBag(ManaCostMatch_instance, $wanted, have));
        break;
      case 5:
        tmp = (have.length < $wanted.length && isSubBag(ManaCostMatch_instance, have, $wanted));
        break;
      default:
        noWhenBranchMatchedException();
        break;
    }
    return tmp;
  };
}
function ManaCostMatch() {
}
protoOf(ManaCostMatch).je = function (value) {
  var symbols = ArrayList_init_$Create$();
  var i = 0;
  $l$loop: while (i < value.length) {
    var ch = charCodeAt(value, i);
    if (ch === _Char___init__impl__6a9atx(123)) {
      var close = indexOf_1(value, _Char___init__impl__6a9atx(125), i + 1 | 0);
      if (close === -1)
        break $l$loop;
      // Inline function 'kotlin.text.uppercase' call
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.collections.plusAssign' call
      var element = substring(value, i + 1 | 0, close).toUpperCase();
      symbols.f(element);
      i = close + 1 | 0;
    } else if (isWhitespace(ch)) {
      i = i + 1 | 0;
    } else if (isDigit(ch)) {
      var j = i;
      while (j < value.length && isDigit(charCodeAt(value, j))) {
        j = j + 1 | 0;
      }
      // Inline function 'kotlin.collections.plusAssign' call
      var element_0 = substring(value, i, j);
      symbols.f(element_0);
      i = j;
    } else if (isLetter(ch)) {
      // Inline function 'kotlin.text.uppercase' call
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.js.unsafeCast' call
      // Inline function 'kotlin.collections.plusAssign' call
      var element_1 = toString(ch).toUpperCase();
      symbols.f(element_1);
      i = i + 1 | 0;
    } else {
      i = i + 1 | 0;
    }
  }
  return symbols;
};
protoOf(ManaCostMatch).ke = function (manaCost) {
  var symbols = ArrayList_init_$Create$();
  var i = indexOf_1(manaCost, _Char___init__impl__6a9atx(123));
  $l$loop: while (!(i === -1)) {
    var close = indexOf_1(manaCost, _Char___init__impl__6a9atx(125), i + 1 | 0);
    if (close === -1)
      break $l$loop;
    if (close > (i + 1 | 0)) {
      // Inline function 'kotlin.text.uppercase' call
      // Inline function 'kotlin.js.asDynamic' call
      // Inline function 'kotlin.collections.plusAssign' call
      var element = substring(manaCost, i + 1 | 0, close).toUpperCase();
      symbols.f(element);
    }
    i = indexOf_1(manaCost, _Char___init__impl__6a9atx(123), close + 1 | 0);
  }
  return symbols;
};
protoOf(ManaCostMatch).le = function (color) {
  switch (color) {
    case 'WHITE':
      return _Char___init__impl__6a9atx(87);
    case 'BLUE':
      return _Char___init__impl__6a9atx(85);
    case 'BLACK':
      return _Char___init__impl__6a9atx(66);
    case 'RED':
      return _Char___init__impl__6a9atx(82);
    case 'GREEN':
      return _Char___init__impl__6a9atx(71);
    default:
      return null;
  }
};
protoOf(ManaCostMatch).me = function (colors) {
  // Inline function 'kotlin.collections.mapNotNull' call
  // Inline function 'kotlin.collections.mapNotNullTo' call
  var destination = ArrayList_init_$Create$();
  // Inline function 'kotlin.collections.forEach' call
  var _iterator__ex2g4s = colors.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    var tmp = this.le(element);
    var tmp0_safe_receiver = tmp == null ? null : new Char(tmp);
    if (tmp0_safe_receiver == null)
      null;
    else {
      // Inline function 'kotlin.let' call
      destination.f(tmp0_safe_receiver);
    }
  }
  return joinToString_0(destination, '');
};
protoOf(ManaCostMatch).ne = function (symbols) {
  var pips = StringBuilder_init_$Create$_0();
  var requirements = ArrayList_init_$Create$();
  var inductionVariable = 0;
  var last = symbols.length;
  $l$loop_1: while (inductionVariable < last) {
    var sym = symbols[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    var halves = split(sym, charArrayOf([_Char___init__impl__6a9atx(47)]));
    var _iterator__ex2g4s = halves.g();
    while (_iterator__ex2g4s.h()) {
      var h = _iterator__ex2g4s.i();
      if (isPip(this, h) && !contains_1(pips, charCodeAt(h, 0))) {
        pips.t5(charCodeAt(h, 0));
      }
    }
    if (halves.j() === 1) {
      if (isPip(this, sym)) {
        // Inline function 'kotlin.collections.plusAssign' call
        requirements.f(sym);
      }
      continue $l$loop_1;
    }
    if (halves.h1('P'))
      continue $l$loop_1;
    var tmp$ret$1;
    $l$block_1: {
      // Inline function 'kotlin.collections.any' call
      var tmp;
      if (isInterface(halves, Collection)) {
        tmp = halves.l();
      } else {
        tmp = false;
      }
      if (tmp) {
        tmp$ret$1 = false;
        break $l$block_1;
      }
      var _iterator__ex2g4s_0 = halves.g();
      while (_iterator__ex2g4s_0.h()) {
        var element = _iterator__ex2g4s_0.i();
        var tmp_0;
        // Inline function 'kotlin.text.isNotEmpty' call
        if (charSequenceLength(element) > 0) {
          var tmp$ret$4;
          $l$block_0: {
            // Inline function 'kotlin.text.all' call
            var inductionVariable_0 = 0;
            while (inductionVariable_0 < charSequenceLength(element)) {
              var element_0 = charSequenceGet(element, inductionVariable_0);
              inductionVariable_0 = inductionVariable_0 + 1 | 0;
              if (!isDigit(element_0)) {
                tmp$ret$4 = false;
                break $l$block_0;
              }
            }
            tmp$ret$4 = true;
          }
          tmp_0 = tmp$ret$4;
        } else {
          tmp_0 = false;
        }
        if (tmp_0) {
          tmp$ret$1 = true;
          break $l$block_1;
        }
      }
      tmp$ret$1 = false;
    }
    if (tmp$ret$1)
      continue $l$loop_1;
    // Inline function 'kotlin.collections.filter' call
    // Inline function 'kotlin.collections.filterTo' call
    var destination = ArrayList_init_$Create$();
    var _iterator__ex2g4s_1 = halves.g();
    while (_iterator__ex2g4s_1.h()) {
      var element_1 = _iterator__ex2g4s_1.i();
      if (isPip(this, element_1)) {
        destination.f(element_1);
      }
    }
    // Inline function 'kotlin.collections.plusAssign' call
    var element_2 = joinToString_0(destination, '');
    requirements.f(element_2);
  }
  var tmp_1 = pips.toString();
  // Inline function 'kotlin.collections.toTypedArray' call
  var tmp$ret$10 = copyToArray(requirements);
  return new CostColors(tmp_1, tmp$ret$10);
};
protoOf(ManaCostMatch).oe = function (cost, identityPips, allowedPips) {
  var inductionVariable = 0;
  var last = identityPips.length;
  while (inductionVariable < last) {
    var pip = charCodeAt(identityPips, inductionVariable);
    inductionVariable = inductionVariable + 1 | 0;
    if (!contains_1(allowedPips, pip) && !contains_1(cost.he_1, pip))
      return false;
  }
  var indexedObject = cost.ie_1;
  var inductionVariable_0 = 0;
  var last_0 = indexedObject.length;
  while (inductionVariable_0 < last_0) {
    var req = indexedObject[inductionVariable_0];
    inductionVariable_0 = inductionVariable_0 + 1 | 0;
    var tmp$ret$0;
    $l$block: {
      // Inline function 'kotlin.text.none' call
      var inductionVariable_1 = 0;
      while (inductionVariable_1 < charSequenceLength(req)) {
        var element = charSequenceGet(req, inductionVariable_1);
        inductionVariable_1 = inductionVariable_1 + 1 | 0;
        if (contains_1(allowedPips, element)) {
          tmp$ret$0 = false;
          break $l$block;
        }
      }
      tmp$ret$0 = true;
    }
    if (tmp$ret$0)
      return false;
  }
  return true;
};
protoOf(ManaCostMatch).pe = function (op, queryValue) {
  // Inline function 'kotlin.collections.toTypedArray' call
  var this_0 = sorted(this.je(queryValue));
  var wanted = copyToArray(this_0);
  return ManaCostMatch$predicate$lambda(op, wanted);
};
var ManaCostMatch_instance;
function ManaCostMatch_getInstance() {
  return ManaCostMatch_instance;
}
function Ok(predicate) {
  this.vd_1 = predicate;
}
protoOf(Ok).toString = function () {
  return 'Ok(predicate=' + toString_1(this.vd_1) + ')';
};
protoOf(Ok).hashCode = function () {
  return hashCode_0(this.vd_1);
};
protoOf(Ok).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Ok))
    return false;
  if (!equals(this.vd_1, other.vd_1))
    return false;
  return true;
};
function Err(message, suggestion) {
  suggestion = suggestion === VOID ? null : suggestion;
  this.td_1 = message;
  this.ud_1 = suggestion;
}
protoOf(Err).toString = function () {
  return 'Err(message=' + this.td_1 + ', suggestion=' + this.ud_1 + ')';
};
protoOf(Err).hashCode = function () {
  var result = getStringHashCode(this.td_1);
  result = imul_0(result, 31) + (this.ud_1 == null ? 0 : getStringHashCode(this.ud_1)) | 0;
  return result;
};
protoOf(Err).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof Err))
    return false;
  if (!(this.td_1 === other.td_1))
    return false;
  if (!(this.ud_1 == other.ud_1))
    return false;
  return true;
};
function ok($this, p) {
  return new Ok(p);
}
function err($this, message, suggestion) {
  suggestion = suggestion === VOID ? null : suggestion;
  return new Err(message, suggestion);
}
function NumericField(label, get) {
  this.qe_1 = label;
  this.re_1 = get;
}
function numericCompare($this, op, a, b) {
  var tmp;
  switch (op.r1_1) {
    case 0:
    case 1:
      tmp = a === b;
      break;
    case 2:
      tmp = !(a === b);
      break;
    case 5:
      tmp = a < b;
      break;
    case 3:
      tmp = a <= b;
      break;
    case 6:
      tmp = a > b;
      break;
    case 4:
      tmp = a >= b;
      break;
    default:
      noWhenBranchMatchedException();
      break;
  }
  return tmp;
}
function symbol($this, op) {
  var tmp;
  switch (op.r1_1) {
    case 0:
    case 1:
      tmp = '=';
      break;
    case 2:
      tmp = '\u2260';
      break;
    case 5:
      tmp = '<';
      break;
    case 3:
      tmp = '\u2264';
      break;
    case 6:
      tmp = '>';
      break;
    case 4:
      tmp = '\u2265';
      break;
    default:
      noWhenBranchMatchedException();
      break;
  }
  return tmp;
}
function numeric($this, aliases, description) {
  var field = getValue($this.uc_1, first(aliases));
  var tmp = Matchers$numeric$lambda(field);
  return entry($this, aliases, $this.rc_1, description, tmp, Matchers$numeric$lambda_0(field));
}
function lowercaseTest($this, atom) {
  if (atom.tb_1)
    return null;
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var needle = atom.sb_1.toLowerCase();
  var tmp;
  if (atom.vb_1) {
    tmp = Matchers$lowercaseTest$lambda(needle);
  } else {
    tmp = Matchers$lowercaseTest$lambda_0(needle);
  }
  return tmp;
}
function anyName($this, c, test) {
  if (test(c.se()))
    return true;
  var printed = c.te();
  var inductionVariable = 0;
  var last = printed.j() - 1 | 0;
  if (inductionVariable <= last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      if (test(printed.k(i)))
        return true;
    }
     while (inductionVariable <= last);
  return false;
}
function anyOf($this, values, test) {
  var inductionVariable = 0;
  var last = values.length;
  while (inductionVariable < last) {
    var v = values[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    if (test(v))
      return true;
  }
  return false;
}
function containsAll($this, have, wanted) {
  var inductionVariable = 0;
  var last = wanted.length;
  while (inductionVariable < last) {
    var w = wanted[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    if (!contains(have, w))
      return false;
  }
  return true;
}
function allIn($this, have, allowed) {
  var inductionVariable = 0;
  var last = have.length;
  while (inductionVariable < last) {
    var h = have[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    if (!contains(allowed, h))
      return false;
  }
  return true;
}
function anyContains($this, values, needle) {
  var inductionVariable = 0;
  var last = values.length;
  while (inductionVariable < last) {
    var v = values[inductionVariable];
    inductionVariable = inductionVariable + 1 | 0;
    if (containsText(v, needle))
      return true;
  }
  return false;
}
function typeWords($this, atom) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp0 = atom.sb_1.toLowerCase();
  // Inline function 'kotlin.text.split' call
  // Inline function 'kotlin.collections.filter' call
  var tmp0_0 = Regex_init_$Create$('\\s+').l7(tmp0, 0);
  // Inline function 'kotlin.collections.filterTo' call
  var destination = ArrayList_init_$Create$();
  var _iterator__ex2g4s = tmp0_0.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    // Inline function 'kotlin.text.isNotEmpty' call
    if (charSequenceLength(element) > 0) {
      destination.f(element);
    }
  }
  return destination;
}
function colorList($this, set) {
  // Inline function 'kotlin.collections.filter' call
  var tmp0 = $this.zc_1;
  // Inline function 'kotlin.collections.filterTo' call
  var destination = ArrayList_init_$Create$();
  var _iterator__ex2g4s = tmp0.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    if (set.h1(element)) {
      destination.f(element);
    }
  }
  // Inline function 'kotlin.collections.map' call
  // Inline function 'kotlin.collections.mapTo' call
  var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(destination, 10));
  var _iterator__ex2g4s_0 = destination.g();
  while (_iterator__ex2g4s_0.h()) {
    var item = _iterator__ex2g4s_0.i();
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp$ret$5 = item.toLowerCase();
    destination_0.f(tmp$ret$5);
  }
  var words = destination_0;
  switch (words.j()) {
    case 0:
      return 'no colours';
    case 1:
      return words.k(0);
    default:
      return joinToString_0(dropLast(words, 1), ', ') + ' and ' + last(words);
  }
}
function color($this, aliases, description, label, atMostByCastability, get) {
  var tmp = Matchers$color$lambda(atMostByCastability, get);
  return entry($this, aliases, $this.sc_1, description, tmp, Matchers$color$lambda_0(label, atMostByCastability));
}
function rarityRank($this, rarity) {
  switch (rarity) {
    case 'COMMON':
      return 1;
    case 'UNCOMMON':
      return 2;
    case 'RARE':
      return 3;
    case 'MYTHIC':
      return 4;
    case 'SPECIAL':
      return 5;
    case 'BONUS':
      return 6;
    default:
      return null;
  }
}
function entry($this, aliases, ops, description, build, explain) {
  return new Matchers$entry$1(aliases, ops, description, build, explain);
}
function levenshtein($this, a, b) {
  if (a === b)
    return 0;
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(a) === 0)
    return b.length;
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(b) === 0)
    return a.length;
  var tmp = 0;
  var tmp_0 = b.length + 1 | 0;
  var tmp_1 = new Int32Array(tmp_0);
  while (tmp < tmp_0) {
    var tmp_2 = tmp;
    tmp_1[tmp_2] = tmp_2;
    tmp = tmp + 1 | 0;
  }
  var prev = tmp_1;
  var curr = new Int32Array(b.length + 1 | 0);
  var inductionVariable = 1;
  var last = a.length;
  if (inductionVariable <= last)
    do {
      var i = inductionVariable;
      inductionVariable = inductionVariable + 1 | 0;
      curr[0] = i;
      var inductionVariable_0 = 1;
      var last_0 = b.length;
      if (inductionVariable_0 <= last_0)
        do {
          var j = inductionVariable_0;
          inductionVariable_0 = inductionVariable_0 + 1 | 0;
          var cost = charCodeAt(a, i - 1 | 0) === charCodeAt(b, j - 1 | 0) ? 0 : 1;
          var tmp0 = curr[j - 1 | 0] + 1 | 0;
          var tmp2 = prev[j] + 1 | 0;
          // Inline function 'kotlin.comparisons.minOf' call
          var c = prev[j - 1 | 0] + cost | 0;
          curr[j] = Math.min(tmp0, tmp2, c);
        }
         while (!(j === last_0));
      // Inline function 'kotlin.collections.copyOf' call
      // Inline function 'kotlin.js.asDynamic' call
      prev = curr.slice();
    }
     while (!(i === last));
  return prev[b.length];
}
function Matchers$ALWAYS_TRUE$lambda(it) {
  return true;
}
function Matchers$ALWAYS_FALSE$lambda(it) {
  return false;
}
function Matchers$FIELDS$lambda(it) {
  return it.ue();
}
function Matchers$FIELDS$lambda_0(it) {
  return it.ue();
}
function Matchers$FIELDS$lambda_1(it) {
  return it.ue();
}
function Matchers$FIELDS$lambda_2(it) {
  return Matchers_getInstance().we(it.ve());
}
function Matchers$FIELDS$lambda_3(it) {
  return Matchers_getInstance().we(it.ve());
}
function Matchers$FIELDS$lambda_4(it) {
  return Matchers_getInstance().we(it.xe());
}
function Matchers$FIELDS$lambda_5(it) {
  return Matchers_getInstance().we(it.xe());
}
function Matchers$FIELDS$lambda_6(it) {
  return Matchers_getInstance().we(it.ye());
}
function Matchers$FIELDS$lambda_7(it) {
  return Matchers_getInstance().we(it.ye());
}
function Matchers$numeric$lambda$lambda($field, $ref, $atom) {
  return function (c) {
    var left = $field.re_1(c);
    var right = $ref.re_1(c);
    return !(left == null) && !(right == null) && numericCompare(Matchers_getInstance(), $atom.rb_1, left, right);
  };
}
function Matchers$numeric$lambda$lambda_0($field, $atom, $n) {
  return function (c) {
    var tmp0_safe_receiver = $field.re_1(c);
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp = numericCompare(Matchers_getInstance(), $atom.rb_1, tmp0_safe_receiver, $n);
    }
    var tmp1_elvis_lhs = tmp;
    return tmp1_elvis_lhs == null ? false : tmp1_elvis_lhs;
  };
}
function Matchers$numeric$lambda($field) {
  return function (atom) {
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.text.trim' call
    var this_0 = atom.sb_1.toLowerCase();
    var v = toString_1(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
    var ref = Matchers_getInstance().uc_1.n1(v);
    var n = toIntOrNull(v);
    var tmp;
    if (!(ref == null)) {
      var tmp_0 = Matchers_getInstance();
      tmp = ok(tmp_0, Matchers$numeric$lambda$lambda($field, ref, atom));
    } else if (n == null) {
      tmp = err(Matchers_getInstance(), 'Expected a number or field reference (cmc/pow/tou/loy), got "' + atom.sb_1 + '".');
    } else {
      var tmp_1 = Matchers_getInstance();
      tmp = ok(tmp_1, Matchers$numeric$lambda$lambda_0($field, atom, n));
    }
    return tmp;
  };
}
function Matchers$numeric$lambda_0($field) {
  return function (atom) {
    var tmp = Matchers_getInstance().uc_1;
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.text.trim' call
    var this_0 = atom.sb_1.toLowerCase();
    var tmp$ret$2 = toString_1(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
    var ref = tmp.n1(tmp$ret$2);
    var tmp_0 = symbol(Matchers_getInstance(), atom.rb_1);
    var tmp1_elvis_lhs = ref == null ? null : ref.qe_1;
    var tmp_1;
    if (tmp1_elvis_lhs == null) {
      // Inline function 'kotlin.text.trim' call
      var this_1 = atom.sb_1;
      tmp_1 = toString_1(trim(isCharSequence(this_1) ? this_1 : THROW_CCE()));
    } else {
      tmp_1 = tmp1_elvis_lhs;
    }
    return $field.qe_1 + ' ' + tmp_0 + ' ' + tmp_1;
  };
}
function Matchers$textTest$lambda($needle) {
  return function (s) {
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    return s.toLowerCase() === $needle;
  };
}
function Matchers$textTest$lambda_0($needle) {
  return function (s) {
    // Inline function 'kotlin.text.lowercase' call
    // Inline function 'kotlin.js.asDynamic' call
    var tmp$ret$0 = s.toLowerCase();
    return containsText(tmp$ret$0, $needle);
  };
}
function Matchers$lowercaseTest$lambda($needle) {
  return function (s) {
    return s === $needle;
  };
}
function Matchers$lowercaseTest$lambda_0($needle) {
  return function (s) {
    return containsText(s, $needle);
  };
}
function Matchers$nameMatcher$lambda(atom) {
  var _destruct__k2r9zo = Matchers_getInstance().ze(atom);
  var test = _destruct__k2r9zo.y8();
  var error = _destruct__k2r9zo.z8();
  var tmp;
  if (test == null) {
    tmp = err(Matchers_getInstance(), ensureNotNull(error));
  } else {
    // Inline function 'kotlin.text.isEmpty' call
    var this_0 = atom.sb_1;
    if (charSequenceLength(this_0) === 0) {
      tmp = ok(Matchers_getInstance(), Matchers_getInstance().pc_1);
    } else {
      var lower = lowercaseTest(Matchers_getInstance(), atom);
      var tmp_0;
      if (!(lower == null)) {
        var tmp_1 = Matchers_getInstance();
        tmp_0 = ok(tmp_1, Matchers$nameMatcher$lambda$lambda(lower));
      } else {
        var tmp_2 = Matchers_getInstance();
        tmp_0 = ok(tmp_2, Matchers$nameMatcher$lambda$lambda_0(test));
      }
      tmp = tmp_0;
    }
  }
  return tmp;
}
function Matchers$nameMatcher$lambda$lambda($lower) {
  return function (c) {
    return anyOf(Matchers_getInstance(), c.af().lf(), $lower);
  };
}
function Matchers$nameMatcher$lambda$lambda_0($test) {
  return function (c) {
    return anyName(Matchers_getInstance(), c, $test);
  };
}
function Matchers$nameMatcher$lambda_0(atom) {
  return atom.tb_1 ? 'name matches /' + atom.sb_1 + '/' : atom.vb_1 ? 'name is "' + atom.sb_1 + '"' : 'name contains "' + atom.sb_1 + '"';
}
function Matchers$oracleMatcher$lambda(atom) {
  var _destruct__k2r9zo = Matchers_getInstance().ze(atom.zb(VOID, VOID, VOID, VOID, VOID, false));
  var test = _destruct__k2r9zo.y8();
  var error = _destruct__k2r9zo.z8();
  var tmp;
  if (test == null) {
    tmp = err(Matchers_getInstance(), ensureNotNull(error));
  } else {
    // Inline function 'kotlin.text.isEmpty' call
    var this_0 = atom.sb_1;
    if (charSequenceLength(this_0) === 0) {
      tmp = ok(Matchers_getInstance(), Matchers_getInstance().pc_1);
    } else {
      var lower = lowercaseTest(Matchers_getInstance(), atom.zb(VOID, VOID, VOID, VOID, VOID, false));
      var tmp_0;
      if (!(lower == null)) {
        var tmp_1 = Matchers_getInstance();
        tmp_0 = ok(tmp_1, Matchers$oracleMatcher$lambda$lambda(lower));
      } else {
        var tmp_2 = Matchers_getInstance();
        tmp_0 = ok(tmp_2, Matchers$oracleMatcher$lambda$lambda_0(test));
      }
      tmp = tmp_0;
    }
  }
  return tmp;
}
function Matchers$oracleMatcher$lambda$lambda($lower) {
  return function (c) {
    return $lower(c.af().mf());
  };
}
function Matchers$oracleMatcher$lambda$lambda_0($test) {
  return function (c) {
    var tmp0_elvis_lhs = c.mf();
    return $test(tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs);
  };
}
function Matchers$oracleMatcher$lambda_0(atom) {
  return atom.tb_1 ? 'rules text matches /' + atom.sb_1 + '/' : 'rules text contains "' + atom.sb_1 + '"';
}
function Matchers$typeMatcher$lambda(atom) {
  var words = typeWords(Matchers_getInstance(), atom);
  var tmp;
  if (words.l()) {
    tmp = ok(Matchers_getInstance(), Matchers_getInstance().pc_1);
  } else {
    // Inline function 'kotlin.collections.toTypedArray' call
    var all = copyToArray(words);
    var tmp_0 = Matchers_getInstance();
    tmp = ok(tmp_0, Matchers$typeMatcher$lambda$lambda(all));
  }
  return tmp;
}
function Matchers$typeMatcher$lambda$lambda($all) {
  return function (c) {
    var ok = true;
    var typeLine = c.af().nf();
    var indexedObject = $all;
    var inductionVariable = 0;
    var last = indexedObject.length;
    $l$loop: while (inductionVariable < last) {
      var w = indexedObject[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      if (!anyContains(Matchers_getInstance(), typeLine, w)) {
        ok = false;
        break $l$loop;
      }
    }
    return ok;
  };
}
function Matchers$typeMatcher$lambda_0(atom) {
  var tmp = typeWords(Matchers_getInstance(), atom);
  return 'type line has ' + joinToString_0(tmp, ' and ', VOID, VOID, VOID, VOID, Matchers$typeMatcher$lambda$lambda_0);
}
function Matchers$typeMatcher$lambda$lambda_0(it) {
  return '"' + it + '"';
}
function Matchers$color$lambda$lambda($atom, $get, $parsed) {
  return function (c) {
    return numericCompare(Matchers_getInstance(), $atom.rb_1, $get(c.af()).length, $parsed.hc_1);
  };
}
function Matchers$color$lambda$lambda_0($get) {
  return function (c) {
    // Inline function 'kotlin.collections.isEmpty' call
    return $get(c.af()).length === 0;
  };
}
function Matchers$color$lambda$lambda_1($get) {
  return function (c) {
    return $get(c.af()).length >= 2;
  };
}
function Matchers$color$lambda$lambda_2($get) {
  return function (c) {
    return $get(c.af()).length === 1;
  };
}
function Matchers$color$lambda$lambda_3($get, $wantedArray) {
  return function (c) {
    return containsAll(Matchers_getInstance(), $get(c.af()), $wantedArray);
  };
}
function Matchers$color$lambda$lambda_4($get, $wanted, $wantedArray) {
  return function (c) {
    var cs = $get(c.af());
    return cs.length === $wanted.j() && containsAll(Matchers_getInstance(), cs, $wantedArray);
  };
}
function Matchers$color$lambda$lambda_5($pips) {
  return function (c) {
    var v = c.af();
    return ManaCostMatch_instance.oe(v.of(), v.pf(), $pips);
  };
}
function Matchers$color$lambda$lambda_6($get, $wantedArray) {
  return function (c) {
    return allIn(Matchers_getInstance(), $get(c.af()), $wantedArray);
  };
}
function Matchers$color$lambda$lambda_7($get, $wanted, $wantedArray) {
  return function (c) {
    var cs = $get(c.af());
    return cs.length > $wanted.j() && containsAll(Matchers_getInstance(), cs, $wantedArray);
  };
}
function Matchers$color$lambda$lambda_8($get, $wanted, $wantedArray) {
  return function (c) {
    var cs = $get(c.af());
    return cs.length < $wanted.j() && allIn(Matchers_getInstance(), cs, $wantedArray);
  };
}
function Matchers$color$lambda($atMostByCastability, $get) {
  return function (atom) {
    var parsed = ColorSugar_getInstance().mc(atom.sb_1);
    var tmp;
    if (parsed instanceof Error_0) {
      tmp = err(Matchers_getInstance(), parsed.ic_1);
    } else {
      if (parsed instanceof Count) {
        var tmp_0 = Matchers_getInstance();
        tmp = ok(tmp_0, Matchers$color$lambda$lambda(atom, $get, parsed));
      } else {
        if (parsed instanceof Colorless) {
          var tmp_1;
          switch (atom.rb_1.r1_1) {
            case 0:
            case 1:
              var tmp_2 = Matchers_getInstance();
              tmp_1 = ok(tmp_2, Matchers$color$lambda$lambda_0($get));
              break;
            default:
              tmp_1 = ok(Matchers_getInstance(), Matchers_getInstance().qc_1);
              break;
          }
          tmp = tmp_1;
        } else {
          if (parsed instanceof Multi) {
            var tmp_3;
            switch (atom.rb_1.r1_1) {
              case 0:
              case 1:
                var tmp_4 = Matchers_getInstance();
                tmp_3 = ok(tmp_4, Matchers$color$lambda$lambda_1($get));
                break;
              default:
                tmp_3 = ok(Matchers_getInstance(), Matchers_getInstance().qc_1);
                break;
            }
            tmp = tmp_3;
          } else {
            if (parsed instanceof Mono) {
              var tmp_5;
              switch (atom.rb_1.r1_1) {
                case 0:
                case 1:
                  var tmp_6 = Matchers_getInstance();
                  tmp_5 = ok(tmp_6, Matchers$color$lambda$lambda_2($get));
                  break;
                default:
                  tmp_5 = ok(Matchers_getInstance(), Matchers_getInstance().qc_1);
                  break;
              }
              tmp = tmp_5;
            } else {
              if (parsed instanceof Colors) {
                var wanted = parsed.gc_1;
                // Inline function 'kotlin.collections.toTypedArray' call
                var wantedArray = copyToArray(wanted);
                var tmp_7;
                switch (atom.rb_1.r1_1) {
                  case 0:
                  case 4:
                    var tmp_8 = Matchers_getInstance();
                    tmp_7 = ok(tmp_8, Matchers$color$lambda$lambda_3($get, wantedArray));
                    break;
                  case 1:
                    var tmp_9 = Matchers_getInstance();
                    tmp_7 = ok(tmp_9, Matchers$color$lambda$lambda_4($get, wanted, wantedArray));
                    break;
                  case 3:
                    var tmp_10;
                    if ($atMostByCastability) {
                      var pips = ManaCostMatch_instance.me(wanted);
                      var tmp_11 = Matchers_getInstance();
                      tmp_10 = ok(tmp_11, Matchers$color$lambda$lambda_5(pips));
                    } else {
                      var tmp_12 = Matchers_getInstance();
                      tmp_10 = ok(tmp_12, Matchers$color$lambda$lambda_6($get, wantedArray));
                    }

                    tmp_7 = tmp_10;
                    break;
                  case 6:
                    var tmp_13 = Matchers_getInstance();
                    tmp_7 = ok(tmp_13, Matchers$color$lambda$lambda_7($get, wanted, wantedArray));
                    break;
                  case 5:
                    var tmp_14 = Matchers_getInstance();
                    tmp_7 = ok(tmp_14, Matchers$color$lambda$lambda_8($get, wanted, wantedArray));
                    break;
                  case 2:
                    tmp_7 = err(Matchers_getInstance(), 'Unsupported operator "' + atom.rb_1.ta_1 + '" on color.');
                    break;
                  default:
                    noWhenBranchMatchedException();
                    break;
                }
                tmp = tmp_7;
              } else {
                noWhenBranchMatchedException();
              }
            }
          }
        }
      }
    }
    return tmp;
  };
}
function Matchers$color$lambda_0($label, $atMostByCastability) {
  return function (atom) {
    var parsed = ColorSugar_getInstance().mc(atom.sb_1);
    var tmp;
    if (parsed instanceof Error_0) {
      tmp = $label + ' is ' + atom.sb_1;
    } else {
      if (parsed instanceof Count) {
        tmp = $label + ' has ' + symbol(Matchers_getInstance(), atom.rb_1) + ' ' + parsed.hc_1 + ' colours';
      } else {
        if (parsed instanceof Colorless) {
          tmp = $label + ' is colourless';
        } else {
          if (parsed instanceof Multi) {
            tmp = $label + ' is multicoloured';
          } else {
            if (parsed instanceof Mono) {
              tmp = $label + ' is a single colour';
            } else {
              if (parsed instanceof Colors) {
                var cs = colorList(Matchers_getInstance(), parsed.gc_1);
                var tmp_0;
                switch (atom.rb_1.r1_1) {
                  case 0:
                  case 4:
                    tmp_0 = $label + ' includes ' + cs;
                    break;
                  case 1:
                    tmp_0 = $label + ' is exactly ' + cs;
                    break;
                  case 3:
                    tmp_0 = $atMostByCastability ? 'playable using only ' + cs : $label + ' is within ' + cs;
                    break;
                  case 6:
                    tmp_0 = $label + ' includes ' + cs + ' and more';
                    break;
                  case 5:
                    tmp_0 = $label + ' is strictly within ' + cs;
                    break;
                  case 2:
                    tmp_0 = $label + ' is not ' + cs;
                    break;
                  default:
                    noWhenBranchMatchedException();
                    break;
                }
                tmp = tmp_0;
              } else {
                noWhenBranchMatchedException();
              }
            }
          }
        }
      }
    }
    return tmp;
  };
}
function Matchers$rarity$lambda(atom) {
  var tmp = Matchers_getInstance().ad_1;
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp$ret$0 = atom.sb_1.toLowerCase();
  var tmp0_elvis_lhs = tmp.n1(tmp$ret$0);
  var tmp_0;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.text.uppercase' call
    // Inline function 'kotlin.js.asDynamic' call
    tmp_0 = atom.sb_1.toUpperCase();
  } else {
    tmp_0 = tmp0_elvis_lhs;
  }
  var target = tmp_0;
  var tmp_1;
  switch (atom.rb_1.r1_1) {
    case 0:
    case 1:
      var tmp_2 = Matchers_getInstance();
      tmp_1 = ok(tmp_2, Matchers$rarity$lambda$lambda(target));
      break;
    case 2:
      var tmp_3 = Matchers_getInstance();
      tmp_1 = ok(tmp_3, Matchers$rarity$lambda$lambda_0(target));
      break;
    case 5:
    case 3:
    case 6:
    case 4:
      var wantedRank = rarityRank(Matchers_getInstance(), target);
      var tmp_4;
      if (wantedRank == null) {
        tmp_4 = err(Matchers_getInstance(), 'Cannot order "' + atom.sb_1 + '" \u2014 known rarities are common/uncommon/rare/mythic.');
      } else {
        var tmp_5 = Matchers_getInstance();
        tmp_4 = ok(tmp_5, Matchers$rarity$lambda$lambda_1(atom, wantedRank));
      }

      tmp_1 = tmp_4;
      break;
    default:
      noWhenBranchMatchedException();
      break;
  }
  return tmp_1;
}
function Matchers$rarity$lambda$lambda($target) {
  return function (c) {
    return c.qf() === $target;
  };
}
function Matchers$rarity$lambda$lambda_0($target) {
  return function (c) {
    return !(c.qf() === $target);
  };
}
function Matchers$rarity$lambda$lambda_1($atom, $wantedRank) {
  return function (c) {
    var tmp0_elvis_lhs = rarityRank(Matchers_getInstance(), c.qf());
    var tmp;
    if (tmp0_elvis_lhs == null) {
      return false;
    } else {
      tmp = tmp0_elvis_lhs;
    }
    var rank = tmp;
    return numericCompare(Matchers_getInstance(), $atom.rb_1, rank, $wantedRank);
  };
}
function Matchers$set$lambda(atom) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  // Inline function 'kotlin.text.trim' call
  var this_0 = atom.sb_1.toLowerCase();
  var target = toString_1(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
  var tmp;
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(target) === 0) {
    tmp = ok(Matchers_getInstance(), Matchers_getInstance().pc_1);
  } else {
    var tmp_0 = Matchers_getInstance();
    tmp = ok(tmp_0, Matchers$set$lambda$lambda(target));
  }
  return tmp;
}
function Matchers$set$lambda$lambda($target) {
  return function (c) {
    var tmp;
    var tmp0_safe_receiver = c.rf();
    var tmp_0;
    if (tmp0_safe_receiver == null) {
      tmp_0 = null;
    } else {
      // Inline function 'kotlin.text.lowercase' call
      // Inline function 'kotlin.js.asDynamic' call
      tmp_0 = tmp0_safe_receiver.toLowerCase();
    }
    if (tmp_0 === $target) {
      tmp = true;
    } else {
      var tmp0 = c.sf();
      var tmp$ret$2;
      $l$block_0: {
        // Inline function 'kotlin.collections.any' call
        var tmp_1;
        if (isInterface(tmp0, Collection)) {
          tmp_1 = tmp0.l();
        } else {
          tmp_1 = false;
        }
        if (tmp_1) {
          tmp$ret$2 = false;
          break $l$block_0;
        }
        var _iterator__ex2g4s = tmp0.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          // Inline function 'kotlin.text.lowercase' call
          // Inline function 'kotlin.js.asDynamic' call
          if (element.toLowerCase() === $target) {
            tmp$ret$2 = true;
            break $l$block_0;
          }
        }
        tmp$ret$2 = false;
      }
      tmp = tmp$ret$2;
    }
    return tmp;
  };
}
function Matchers$format$lambda(atom) {
  // Inline function 'kotlin.text.uppercase' call
  // Inline function 'kotlin.js.asDynamic' call
  // Inline function 'kotlin.text.trim' call
  var this_0 = atom.sb_1.toUpperCase();
  var target = toString_1(trim(isCharSequence(this_0) ? this_0 : THROW_CCE()));
  var tmp;
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(target) === 0) {
    tmp = ok(Matchers_getInstance(), Matchers_getInstance().pc_1);
  } else {
    var tmp_0 = Matchers_getInstance();
    tmp = ok(tmp_0, Matchers$format$lambda$lambda(target));
  }
  return tmp;
}
function Matchers$format$lambda$lambda($target) {
  return function (c) {
    return c.tf().h1($target);
  };
}
function Matchers$keyword$lambda(atom) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp0 = atom.sb_1.toLowerCase();
  // Inline function 'kotlin.text.replace' call
  var target = Regex_init_$Create$('\\s+').d7(tmp0, '_');
  var tmp;
  // Inline function 'kotlin.text.isEmpty' call
  if (charSequenceLength(target) === 0) {
    tmp = ok(Matchers_getInstance(), Matchers_getInstance().pc_1);
  } else {
    var tmp_0 = Matchers_getInstance();
    tmp = ok(tmp_0, Matchers$keyword$lambda$lambda(target));
  }
  return tmp;
}
function Matchers$keyword$lambda$lambda($target) {
  return function (c) {
    return contains(c.af().uf(), $target);
  };
}
function Matchers$layout$lambda(atom) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp;
  switch (atom.sb_1.toLowerCase()) {
    case 'transform':
    case 'mdfc':
    case 'modal_dfc':
    case 'dfc':
    case 'doublefaced':
    case 'double_faced':
      var tmp_0 = Matchers_getInstance();
      tmp = ok(tmp_0, Matchers$layout$lambda$lambda);
      break;
    case 'normal':
      var tmp_1 = Matchers_getInstance();
      tmp = ok(tmp_1, Matchers$layout$lambda$lambda_0);
      break;
    default:
      tmp = err(Matchers_getInstance(), 'Unknown layout "' + atom.sb_1 + '". Try transform / mdfc / normal.');
      break;
  }
  return tmp;
}
function Matchers$layout$lambda$lambda(c) {
  return c.vf();
}
function Matchers$layout$lambda$lambda_0(c) {
  return !c.vf();
}
function Matchers$isMatcher$lambda(atom) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var v = atom.sb_1.toLowerCase();
  var tmp;
  if (v === 'land') {
    var tmp_0 = Matchers_getInstance();
    tmp = ok(tmp_0, Matchers$isMatcher$lambda$lambda);
  } else {
    if (v === 'creature') {
      var tmp_1 = Matchers_getInstance();
      tmp = ok(tmp_1, Matchers$isMatcher$lambda$lambda_0);
    } else {
      if (v === 'instant') {
        var tmp_2 = Matchers_getInstance();
        tmp = ok(tmp_2, Matchers$isMatcher$lambda$lambda_1);
      } else {
        if (v === 'sorcery') {
          var tmp_3 = Matchers_getInstance();
          tmp = ok(tmp_3, Matchers$isMatcher$lambda$lambda_2);
        } else {
          if (v === 'enchantment') {
            var tmp_4 = Matchers_getInstance();
            tmp = ok(tmp_4, Matchers$isMatcher$lambda$lambda_3);
          } else {
            if (v === 'artifact') {
              var tmp_5 = Matchers_getInstance();
              tmp = ok(tmp_5, Matchers$isMatcher$lambda$lambda_4);
            } else {
              if (v === 'planeswalker') {
                var tmp_6 = Matchers_getInstance();
                tmp = ok(tmp_6, Matchers$isMatcher$lambda$lambda_5);
              } else {
                if (v === 'kindred') {
                  var tmp_7 = Matchers_getInstance();
                  tmp = ok(tmp_7, Matchers$isMatcher$lambda$lambda_6);
                } else {
                  if (v === 'permanent') {
                    var tmp_8 = Matchers_getInstance();
                    tmp = ok(tmp_8, Matchers$isMatcher$lambda$lambda_7);
                  } else {
                    if (v === 'spell') {
                      var tmp_9 = Matchers_getInstance();
                      tmp = ok(tmp_9, Matchers$isMatcher$lambda$lambda_8);
                    } else {
                      if (v === 'legendary') {
                        var tmp_10 = Matchers_getInstance();
                        tmp = ok(tmp_10, Matchers$isMatcher$lambda$lambda_9);
                      } else {
                        if (v === 'basic') {
                          var tmp_11 = Matchers_getInstance();
                          tmp = ok(tmp_11, Matchers$isMatcher$lambda$lambda_10);
                        } else {
                          if (v === 'colorless') {
                            var tmp_12 = Matchers_getInstance();
                            tmp = ok(tmp_12, Matchers$isMatcher$lambda$lambda_11);
                          } else {
                            switch (v) {
                              case 'multicolor':
                              case 'multicolour':
                                var tmp_13 = Matchers_getInstance();
                                tmp = ok(tmp_13, Matchers$isMatcher$lambda$lambda_12);
                                break;
                              default:
                                switch (v) {
                                  case 'monocolor':
                                  case 'monocolour':
                                  case 'mono':
                                    var tmp_14 = Matchers_getInstance();
                                    tmp = ok(tmp_14, Matchers$isMatcher$lambda$lambda_13);
                                    break;
                                  default:
                                    switch (v) {
                                      case 'dfc':
                                      case 'mdfc':
                                      case 'transform':
                                      case 'doublefaced':
                                      case 'double_faced':
                                        var tmp_15 = Matchers_getInstance();
                                        tmp = ok(tmp_15, Matchers$isMatcher$lambda$lambda_14);
                                        break;
                                      default:
                                        if (v === 'vanilla') {
                                          var tmp_16 = Matchers_getInstance();
                                          tmp = ok(tmp_16, Matchers$isMatcher$lambda$lambda_15);
                                        } else {
                                          switch (v) {
                                            case 'frenchvanilla':
                                            case 'french_vanilla':
                                              var tmp_17 = Matchers_getInstance();
                                              tmp = ok(tmp_17, Matchers$isMatcher$lambda$lambda_16);
                                              break;
                                            default:
                                              if (v === 'bear') {
                                                var tmp_18 = Matchers_getInstance();
                                                tmp = ok(tmp_18, Matchers$isMatcher$lambda$lambda_17);
                                              } else {
                                                if (v === 'historic') {
                                                  var tmp_19 = Matchers_getInstance();
                                                  tmp = ok(tmp_19, Matchers$isMatcher$lambda$lambda_18);
                                                } else {
                                                  // Inline function 'kotlin.collections.contains' call
                                                  // Inline function 'kotlin.collections.containsKey' call
                                                  var this_0 = Matchers_getInstance().gd_1;
                                                  if ((isInterface(this_0, KtMap) ? this_0 : THROW_CCE()).l1(v)) {
                                                    tmp = err(Matchers_getInstance(), '`is:' + v + '` is not yet supported (' + Matchers_getInstance().gd_1.n1(v) + ').');
                                                  } else {
                                                    tmp = Matchers_getInstance().ed_1(atom.zb(VOID, VOID, v));
                                                  }
                                                }
                                              }

                                              break;
                                          }
                                        }

                                        break;
                                    }

                                    break;
                                }

                                break;
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  return tmp;
}
function Matchers$isMatcher$lambda$lambda(c) {
  return c.wf().h1('LAND');
}
function Matchers$isMatcher$lambda$lambda_0(c) {
  return c.wf().h1('CREATURE');
}
function Matchers$isMatcher$lambda$lambda_1(c) {
  return c.wf().h1('INSTANT');
}
function Matchers$isMatcher$lambda$lambda_2(c) {
  return c.wf().h1('SORCERY');
}
function Matchers$isMatcher$lambda$lambda_3(c) {
  return c.wf().h1('ENCHANTMENT');
}
function Matchers$isMatcher$lambda$lambda_4(c) {
  return c.wf().h1('ARTIFACT');
}
function Matchers$isMatcher$lambda$lambda_5(c) {
  return c.wf().h1('PLANESWALKER');
}
function Matchers$isMatcher$lambda$lambda_6(c) {
  return c.wf().h1('KINDRED');
}
function Matchers$isMatcher$lambda$lambda_7(c) {
  return c.wf().h1('CREATURE') || c.wf().h1('LAND') || c.wf().h1('ARTIFACT') || c.wf().h1('ENCHANTMENT') || c.wf().h1('PLANESWALKER');
}
function Matchers$isMatcher$lambda$lambda_8(c) {
  return !c.wf().h1('LAND');
}
function Matchers$isMatcher$lambda$lambda_9(c) {
  return c.xf().h1('LEGENDARY');
}
function Matchers$isMatcher$lambda$lambda_10(c) {
  return c.yf();
}
function Matchers$isMatcher$lambda$lambda_11(c) {
  // Inline function 'kotlin.collections.isEmpty' call
  return c.af().zf().length === 0;
}
function Matchers$isMatcher$lambda$lambda_12(c) {
  return c.af().zf().length >= 2;
}
function Matchers$isMatcher$lambda$lambda_13(c) {
  return c.af().zf().length === 1;
}
function Matchers$isMatcher$lambda$lambda_14(c) {
  return c.vf();
}
function Matchers$isMatcher$lambda$lambda_15(c) {
  var tmp;
  if (c.wf().h1('CREATURE')) {
    // Inline function 'kotlin.text.isNullOrBlank' call
    var this_0 = c.mf();
    tmp = this_0 == null || isBlank(this_0);
  } else {
    tmp = false;
  }
  return tmp;
}
function Matchers$isMatcher$lambda$lambda_16(c) {
  var tmp;
  if (c.wf().h1('CREATURE')) {
    // Inline function 'kotlin.collections.isNotEmpty' call
    tmp = !c.uf().l();
  } else {
    tmp = false;
  }
  return tmp;
}
function Matchers$isMatcher$lambda$lambda_17(c) {
  return c.wf().h1('CREATURE') && c.ue() === 2 && Matchers_getInstance().we(c.ve()) === 2 && Matchers_getInstance().we(c.xe()) === 2;
}
function Matchers$isMatcher$lambda$lambda_18(c) {
  return c.xf().h1('LEGENDARY') || c.wf().h1('ARTIFACT') || c.ag().h1('SAGA');
}
function Matchers$mana$lambda(atom) {
  var test = ManaCostMatch_instance.pe(atom.rb_1, atom.sb_1);
  var tmp = Matchers_getInstance();
  return ok(tmp, Matchers$mana$lambda$lambda(test));
}
function Matchers$mana$lambda$lambda($test) {
  return function (c) {
    return $test(c.af().bg());
  };
}
function Matchers$ENTRIES$lambda(it) {
  return it.zf();
}
function Matchers$ENTRIES$lambda_0(it) {
  return it.cg();
}
function Matchers$ENTRIES$lambda_1(atom) {
  var tmp = ManaCostMatch_instance.je(atom.sb_1);
  var cost = joinToString_0(tmp, '', VOID, VOID, VOID, VOID, Matchers$ENTRIES$lambda$lambda);
  var tmp_0;
  switch (atom.rb_1.r1_1) {
    case 0:
    case 1:
      tmp_0 = 'mana cost is ' + cost;
      break;
    case 2:
      tmp_0 = 'mana cost is not ' + cost;
      break;
    case 4:
      tmp_0 = 'mana cost contains ' + cost;
      break;
    case 6:
      tmp_0 = 'mana cost contains ' + cost + ' and more';
      break;
    case 3:
      tmp_0 = 'mana cost is within ' + cost;
      break;
    case 5:
      tmp_0 = 'mana cost is strictly within ' + cost;
      break;
    default:
      noWhenBranchMatchedException();
      break;
  }
  return tmp_0;
}
function Matchers$ENTRIES$lambda$lambda(it) {
  return '{' + it + '}';
}
function Matchers$ENTRIES$lambda_2(atom) {
  var tmp = Matchers_getInstance().ad_1;
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var tmp$ret$0 = atom.sb_1.toLowerCase();
  var tmp0_elvis_lhs = tmp.n1(tmp$ret$0);
  var tmp_0;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.text.uppercase' call
    // Inline function 'kotlin.js.asDynamic' call
    tmp_0 = atom.sb_1.toUpperCase();
  } else {
    tmp_0 = tmp0_elvis_lhs;
  }
  var target = tmp_0;
  var tmp_1 = symbol(Matchers_getInstance(), atom.rb_1);
  var tmp1_elvis_lhs = Matchers_getInstance().kd_1.n1(target);
  return 'rarity ' + tmp_1 + ' ' + (tmp1_elvis_lhs == null ? atom.sb_1 : tmp1_elvis_lhs);
}
function Matchers$ENTRIES$lambda_3(atom) {
  // Inline function 'kotlin.text.trim' call
  var this_0 = atom.sb_1;
  // Inline function 'kotlin.text.uppercase' call
  // Inline function 'kotlin.js.asDynamic' call
  return 'printed in ' + toString_1(trim(isCharSequence(this_0) ? this_0 : THROW_CCE())).toUpperCase();
}
function Matchers$ENTRIES$lambda_4(atom) {
  // Inline function 'kotlin.text.trim' call
  var this_0 = atom.sb_1;
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  // Inline function 'kotlin.text.replaceFirstChar' call
  var this_1 = toString_1(trim(isCharSequence(this_0) ? this_0 : THROW_CCE())).toLowerCase();
  var tmp;
  // Inline function 'kotlin.text.isNotEmpty' call
  if (charSequenceLength(this_1) > 0) {
    // Inline function 'kotlin.text.uppercase' call
    var this_2 = charCodeAt(this_1, 0);
    // Inline function 'kotlin.js.asDynamic' call
    // Inline function 'kotlin.js.unsafeCast' call
    var tmp$ret$5 = toString(this_2).toUpperCase();
    tmp = toString_1(tmp$ret$5) + substring_0(this_1, 1);
  } else {
    tmp = this_1;
  }
  return 'legal in ' + tmp;
}
function Matchers$ENTRIES$lambda_5(atom) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  return 'has ' + atom.sb_1.toLowerCase();
}
function Matchers$ENTRIES$lambda_6(atom) {
  var tmp;
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  if (atom.sb_1.toLowerCase() === 'normal') {
    tmp = 'is single-faced';
  } else {
    tmp = 'is double-faced';
  }
  return tmp;
}
function Matchers$ENTRIES$lambda_7(atom) {
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var v = atom.sb_1.toLowerCase();
  var tmp0_elvis_lhs = Matchers_getInstance().jd_1.n1(v);
  return tmp0_elvis_lhs == null ? 'has ' + v : tmp0_elvis_lhs;
}
function Matchers$entry$1($aliases, $ops, $description, $build, $explain) {
  this.gg_1 = $build;
  this.hg_1 = $explain;
  this.dg_1 = $aliases;
  this.eg_1 = $ops;
  this.fg_1 = $description;
}
protoOf(Matchers$entry$1).ig = function () {
  return this.dg_1;
};
protoOf(Matchers$entry$1).rd = function () {
  return this.eg_1;
};
protoOf(Matchers$entry$1).jg = function () {
  return this.fg_1;
};
protoOf(Matchers$entry$1).sd = function (atom) {
  return this.gg_1(atom);
};
protoOf(Matchers$entry$1).kg = function (atom) {
  return this.hg_1(atom);
};
function Matchers() {
  Matchers_instance = this;
  var tmp = this;
  tmp.pc_1 = Matchers$ALWAYS_TRUE$lambda;
  var tmp_0 = this;
  tmp_0.qc_1 = Matchers$ALWAYS_FALSE$lambda;
  this.rc_1 = listOf_0([Op_EQ_getInstance(), Op_EXACT_getInstance(), Op_NEQ_getInstance(), Op_LE_getInstance(), Op_GE_getInstance(), Op_LT_getInstance(), Op_GT_getInstance()]);
  this.sc_1 = listOf_0([Op_EQ_getInstance(), Op_EXACT_getInstance(), Op_LE_getInstance(), Op_GE_getInstance(), Op_LT_getInstance(), Op_GT_getInstance()]);
  this.tc_1 = listOf(Op_EQ_getInstance());
  var tmp_1 = this;
  var tmp_2 = to('cmc', new NumericField('mana value', Matchers$FIELDS$lambda));
  var tmp_3 = to('mv', new NumericField('mana value', Matchers$FIELDS$lambda_0));
  var tmp_4 = to('manavalue', new NumericField('mana value', Matchers$FIELDS$lambda_1));
  var tmp_5 = to('pow', new NumericField('power', Matchers$FIELDS$lambda_2));
  var tmp_6 = to('power', new NumericField('power', Matchers$FIELDS$lambda_3));
  var tmp_7 = to('tou', new NumericField('toughness', Matchers$FIELDS$lambda_4));
  var tmp_8 = to('toughness', new NumericField('toughness', Matchers$FIELDS$lambda_5));
  var tmp_9 = to('loy', new NumericField('loyalty', Matchers$FIELDS$lambda_6));
  tmp_1.uc_1 = mapOf([tmp_2, tmp_3, tmp_4, tmp_5, tmp_6, tmp_7, tmp_8, tmp_9, to('loyalty', new NumericField('loyalty', Matchers$FIELDS$lambda_7))]);
  this.vc_1 = 'ims';
  var tmp_10 = this;
  var tmp_11 = listOf_0(['name', 'n']);
  var tmp_12 = Matchers$nameMatcher$lambda;
  tmp_10.wc_1 = entry(this, tmp_11, this.tc_1, 'Card name', tmp_12, Matchers$nameMatcher$lambda_0);
  var tmp_13 = this;
  var tmp_14 = listOf_0(['oracle', 'o']);
  var tmp_15 = Matchers$oracleMatcher$lambda;
  tmp_13.xc_1 = entry(this, tmp_14, this.tc_1, 'Rules text', tmp_15, Matchers$oracleMatcher$lambda_0);
  var tmp_16 = this;
  var tmp_17 = listOf_0(['type', 't']);
  var tmp_18 = Matchers$typeMatcher$lambda;
  tmp_16.yc_1 = entry(this, tmp_17, this.tc_1, 'Type line \u2014 every word must appear', tmp_18, Matchers$typeMatcher$lambda_0);
  this.zc_1 = listOf_0(['WHITE', 'BLUE', 'BLACK', 'RED', 'GREEN']);
  this.ad_1 = mapOf([to('c', 'COMMON'), to('common', 'COMMON'), to('u', 'UNCOMMON'), to('uncommon', 'UNCOMMON'), to('r', 'RARE'), to('rare', 'RARE'), to('m', 'MYTHIC'), to('mythic', 'MYTHIC'), to('s', 'SPECIAL'), to('special', 'SPECIAL'), to('bonus', 'BONUS')]);
  var tmp_19 = this;
  tmp_19.bd_1 = Matchers$rarity$lambda;
  var tmp_20 = this;
  tmp_20.cd_1 = Matchers$set$lambda;
  var tmp_21 = this;
  tmp_21.dd_1 = Matchers$format$lambda;
  var tmp_22 = this;
  tmp_22.ed_1 = Matchers$keyword$lambda;
  var tmp_23 = this;
  tmp_23.fd_1 = Matchers$layout$lambda;
  this.gd_1 = mapOf([to('reprint', 'print metadata not stamped on the catalog'), to('firstprint', 'print metadata not stamped on the catalog'), to('unique', 'print metadata not stamped on the catalog'), to('hybrid', 'mana-symbol metadata not parsed'), to('phyrexian', 'mana-symbol metadata not parsed'), to('split', 'split-card layout not modelled'), to('flip', 'flip-card layout not modelled'), to('meld', 'meld-card layout not modelled'), to('leveler', 'leveler-card layout not modelled'), to('modal', 'modal-spell metadata not surfaced'), to('party', 'party-tribe metadata not surfaced'), to('outlaw', 'outlaw-supertypes metadata not surfaced'), to('manland', 'land-cycle metadata not surfaced'), to('bikeland', 'land-cycle metadata not surfaced'), to('cycleland', 'land-cycle metadata not surfaced'), to('checkland', 'land-cycle metadata not surfaced'), to('shockland', 'land-cycle metadata not surfaced'), to('fetchland', 'land-cycle metadata not surfaced'), to('painland', 'land-cycle metadata not surfaced'), to('scryland', 'land-cycle metadata not surfaced'), to('fastland', 'land-cycle metadata not surfaced'), to('slowland', 'land-cycle metadata not surfaced'), to('filterland', 'land-cycle metadata not surfaced'), to('gainland', 'land-cycle metadata not surfaced'), to('triland', 'land-cycle metadata not surfaced'), to('triome', 'land-cycle metadata not surfaced'), to('creatureland', 'land-cycle metadata not surfaced'), to('dual', 'land-cycle metadata not surfaced'), to('funny', 'set-style metadata not surfaced'), to('digital', 'print game-mode not surfaced'), to('alchemy', 'print game-mode not surfaced'), to('rebalanced', 'print game-mode not surfaced'), to('promo', 'print metadata not surfaced'), to('spotlight', 'print metadata not surfaced'), to('scryfallpreview', 'print metadata not surfaced'), to('foil', 'physical printing not surfaced'), to('nonfoil', 'physical printing not surfaced'), to('etched', 'physical printing not surfaced'), to('glossy', 'physical printing not surfaced'), to('hires', 'image metadata not surfaced'), to('universesbeyond', 'set-style metadata not surfaced'), to('default', 'print-prefer metadata not surfaced'), to('atypical', 'print-prefer metadata not surfaced'), to('old', 'border metadata not surfaced'), to('new', 'border metadata not surfaced'), to('datestamped', 'print metadata not surfaced'), to('booster', 'set metadata not surfaced'), to('league', 'set metadata not surfaced'), to('buyabox', 'set metadata not surfaced'), to('giftbox', 'set metadata not surfaced'), to('intro_pack', 'set metadata not surfaced'), to('gameday', 'set metadata not surfaced'), to('prerelease', 'set metadata not surfaced'), to('release', 'set metadata not surfaced'), to('fnm', 'set metadata not surfaced'), to('judge_gift', 'set metadata not surfaced'), to('arena_league', 'set metadata not surfaced'), to('player_rewards', 'set metadata not surfaced'), to('media_insert', 'set metadata not surfaced'), to('instore', 'set metadata not surfaced'), to('convention', 'set metadata not surfaced'), to('set_promo', 'set metadata not surfaced'), to('planeswalker_deck', 'set metadata not surfaced'), to('commander', 'ambiguous (use t:legendary t:creature instead)'), to('brawler', 'format-eligibility metadata not surfaced'), to('companion', 'format-eligibility metadata not surfaced'), to('duelcommander', 'format-eligibility metadata not surfaced'), to('oathbreaker', 'format-eligibility metadata not surfaced'), to('partner', 'format-eligibility metadata not surfaced'), to('gamechanger', 'format-eligibility metadata not surfaced'), to('reserved', 'reserved-list metadata not surfaced'), to('tdfc', 'DFC layout subtype not surfaced (use is:dfc)'), to('meldpart', 'meld-card layout not modelled'), to('meldresult', 'meld-card layout not modelled'), to('newinpauper', 'format-eligibility delta not surfaced')]);
  var tmp_24 = this;
  tmp_24.hd_1 = Matchers$isMatcher$lambda;
  var tmp_25 = this;
  tmp_25.id_1 = Matchers$mana$lambda;
  this.jd_1 = mapOf([to('land', 'is a land'), to('creature', 'is a creature'), to('instant', 'is an instant'), to('sorcery', 'is a sorcery'), to('enchantment', 'is an enchantment'), to('artifact', 'is an artifact'), to('planeswalker', 'is a planeswalker'), to('kindred', 'is kindred'), to('permanent', 'is a permanent'), to('spell', 'is a spell (not a land)'), to('legendary', 'is legendary'), to('basic', 'is a basic land'), to('colorless', 'colour identity is colourless'), to('multicolor', 'is multicoloured'), to('multicolour', 'is multicoloured'), to('monocolor', 'is a single colour'), to('monocolour', 'is a single colour'), to('mono', 'is a single colour'), to('dfc', 'is double-faced'), to('mdfc', 'is double-faced'), to('transform', 'is double-faced'), to('doublefaced', 'is double-faced'), to('double_faced', 'is double-faced'), to('vanilla', 'is a creature with no rules text'), to('frenchvanilla', 'is a creature with keyword abilities'), to('french_vanilla', 'is a creature with keyword abilities'), to('bear', 'is a 2-mana 2/2 creature'), to('historic', 'is historic')]);
  this.kd_1 = mapOf([to('COMMON', 'common'), to('UNCOMMON', 'uncommon'), to('RARE', 'rare'), to('MYTHIC', 'mythic'), to('SPECIAL', 'special'), to('BONUS', 'bonus')]);
  var tmp_26 = this;
  var tmp_27 = listOf_0(['c', 'color', 'colour', 'id', 'identity']);
  var tmp_28 = color(this, tmp_27, 'Colour identity', 'colour identity', true, Matchers$ENTRIES$lambda);
  var tmp_29 = listOf('cost');
  var tmp_30 = color(this, tmp_29, 'Colours in the mana cost', 'mana cost colour', false, Matchers$ENTRIES$lambda_0);
  var tmp_31 = listOf_0(['mana', 'm']);
  var tmp_32 = entry(this, tmp_31, this.rc_1, 'Mana cost, e.g. {2}{U}', this.id_1, Matchers$ENTRIES$lambda_1);
  var tmp_33 = numeric(this, listOf_0(['cmc', 'mv', 'manavalue']), 'Mana value');
  var tmp_34 = numeric(this, listOf_0(['pow', 'power']), 'Power');
  var tmp_35 = numeric(this, listOf_0(['tou', 'toughness']), 'Toughness');
  var tmp_36 = numeric(this, listOf_0(['loy', 'loyalty']), 'Starting loyalty');
  var tmp_37 = listOf_0(['r', 'rarity']);
  var tmp_38 = entry(this, tmp_37, this.rc_1, 'Rarity: common, uncommon, rare, mythic', this.bd_1, Matchers$ENTRIES$lambda_2);
  var tmp_39 = listOf_0(['s', 'set', 'e', 'edition']);
  var tmp_40 = entry(this, tmp_39, this.tc_1, 'Set code, e.g. blb', this.cd_1, Matchers$ENTRIES$lambda_3);
  var tmp_41 = listOf_0(['f', 'format', 'legal']);
  var tmp_42 = entry(this, tmp_41, this.tc_1, 'Legal in a format, e.g. standard', this.dd_1, Matchers$ENTRIES$lambda_4);
  var tmp_43 = listOf_0(['kw', 'keyword']);
  var tmp_44 = entry(this, tmp_43, this.tc_1, 'Keyword ability, e.g. flying', this.ed_1, Matchers$ENTRIES$lambda_5);
  var tmp_45 = listOf('layout');
  var tmp_46 = entry(this, tmp_45, this.tc_1, 'Layout: normal or double-faced', this.fd_1, Matchers$ENTRIES$lambda_6);
  var tmp_47 = listOf('is');
  tmp_26.ld_1 = listOf_0([this.wc_1, this.xc_1, this.yc_1, tmp_28, tmp_30, tmp_32, tmp_33, tmp_34, tmp_35, tmp_36, tmp_38, tmp_40, tmp_42, tmp_44, tmp_46, entry(this, tmp_47, this.tc_1, 'Shortcuts: land, creature, permanent, legendary, vanilla, dfc, \u2026', this.hd_1, Matchers$ENTRIES$lambda_7)]);
  var tmp_48 = this;
  // Inline function 'kotlin.collections.buildMap' call
  // Inline function 'kotlin.collections.buildMapInternal' call
  // Inline function 'kotlin.apply' call
  var this_0 = LinkedHashMap_init_$Create$();
  var _iterator__ex2g4s = Matchers_getInstance().ld_1.g();
  while (_iterator__ex2g4s.h()) {
    var e = _iterator__ex2g4s.i();
    var _iterator__ex2g4s_0 = e.ig().g();
    while (_iterator__ex2g4s_0.h()) {
      var a = _iterator__ex2g4s_0.i();
      this_0.w2(a, e);
    }
  }
  tmp_48.md_1 = this_0.d6();
  var tmp_49 = this;
  // Inline function 'kotlin.collections.flatMap' call
  var tmp0 = this.ld_1;
  // Inline function 'kotlin.collections.flatMapTo' call
  var destination = ArrayList_init_$Create$();
  var _iterator__ex2g4s_1 = tmp0.g();
  while (_iterator__ex2g4s_1.h()) {
    var element = _iterator__ex2g4s_1.i();
    var list = element.ig();
    addAll(destination, list);
  }
  tmp_49.nd_1 = sorted(distinct(destination));
}
protoOf(Matchers).we = function (s) {
  // Inline function 'kotlin.text.isNullOrEmpty' call
  if (s == null || charSequenceLength(s) === 0)
    return null;
  var i = charCodeAt(s, 0) === _Char___init__impl__6a9atx(45) ? 1 : 0;
  var n = 0;
  var start = i;
  $l$loop: while (true) {
    var tmp;
    if (i < s.length) {
      var containsArg = charCodeAt(s, i);
      tmp = _Char___init__impl__6a9atx(48) <= containsArg ? containsArg <= _Char___init__impl__6a9atx(57) : false;
    } else {
      tmp = false;
    }
    if (!tmp) {
      break $l$loop;
    }
    n = imul_0(n, 10) + Char__minus_impl_a2frrh(charCodeAt(s, i), _Char___init__impl__6a9atx(48)) | 0;
    i = i + 1 | 0;
  }
  if (i === start)
    return null;
  return charCodeAt(s, 0) === _Char___init__impl__6a9atx(45) ? -n | 0 : n;
};
protoOf(Matchers).ze = function (atom) {
  if (atom.tb_1) {
    // Inline function 'kotlin.text.filter' call
    var tmp0 = atom.ub_1;
    // Inline function 'kotlin.text.filterTo' call
    var destination = StringBuilder_init_$Create$_0();
    var inductionVariable = 0;
    var last = charSequenceLength(tmp0);
    if (inductionVariable < last)
      do {
        var index = inductionVariable;
        inductionVariable = inductionVariable + 1 | 0;
        var element = charSequenceGet(tmp0, index);
        if (!contains_1('ims', element)) {
          destination.t5(element);
        }
      }
       while (inductionVariable < last);
    var bad = destination.toString();
    // Inline function 'kotlin.text.isNotEmpty' call
    if (charSequenceLength(bad) > 0)
      return to(null, 'Unknown regex flag "' + toString(first_1(bad)) + '" \u2014 use i, m or s.');
    // Inline function 'kotlin.text.ifEmpty' call
    var this_0 = atom.ub_1;
    var tmp;
    // Inline function 'kotlin.text.isEmpty' call
    if (charSequenceLength(this_0) === 0) {
      tmp = 'i';
    } else {
      tmp = this_0;
    }
    var flags = tmp;
    var tmp_0;
    try {
      tmp_0 = to(compileQueryRegex(atom.sb_1, flags), null);
    } catch ($p) {
      var tmp_1;
      if ($p instanceof IllegalArgumentException) {
        var e = $p;
        tmp_1 = to(null, 'Invalid regex: ' + e.message + '.');
      } else {
        throw $p;
      }
      tmp_0 = tmp_1;
    }
    return tmp_0;
  }
  // Inline function 'kotlin.text.lowercase' call
  // Inline function 'kotlin.js.asDynamic' call
  var needle = atom.sb_1.toLowerCase();
  if (atom.vb_1) {
    return to(Matchers$textTest$lambda(needle), null);
  }
  return to(Matchers$textTest$lambda_0(needle), null);
};
protoOf(Matchers).od = function (unknown) {
  var best = null;
  var bestDist = 2147483647;
  var _iterator__ex2g4s = this.nd_1.g();
  while (_iterator__ex2g4s.h()) {
    var k = _iterator__ex2g4s.i();
    var d = levenshtein(this, unknown, k);
    if (d < bestDist) {
      bestDist = d;
      best = k;
    }
  }
  return bestDist <= 2 ? best : null;
};
var Matchers_instance;
function Matchers_getInstance() {
  if (Matchers_instance == null)
    new Matchers();
  return Matchers_instance;
}
var static_init_called_0;
function static_init_0() {
  if (static_init_called_0)
    return Unit_instance;
  static_init_called_0 = true;
  NodeKind_AND_instance = new NodeKind('AND', 0);
  NodeKind_OR_instance = new NodeKind('OR', 1);
}
var NodeKind_AND_instance;
var NodeKind_OR_instance;
function ParseAst(ast, errors) {
  this.lg_1 = ast;
  this.mg_1 = errors;
}
protoOf(ParseAst).toString = function () {
  return 'ParseAst(ast=' + toString_0(this.lg_1) + ', errors=' + toString_1(this.mg_1) + ')';
};
protoOf(ParseAst).hashCode = function () {
  var result = this.lg_1 == null ? 0 : hashCode_0(this.lg_1);
  result = imul_0(result, 31) + hashCode_0(this.mg_1) | 0;
  return result;
};
protoOf(ParseAst).equals = function (other) {
  if (this === other)
    return true;
  if (!(other instanceof ParseAst))
    return false;
  if (!equals(this.lg_1, other.lg_1))
    return false;
  if (!equals(this.mg_1, other.mg_1))
    return false;
  return true;
};
function State(tokens) {
  this.ng_1 = tokens;
  this.og_1 = 0;
  this.pg_1 = ArrayList_init_$Create$();
}
function parseOr($this, state) {
  var left = parseAnd($this, state);
  $l$loop_0: while (state.og_1 < state.ng_1.j()) {
    var tok = state.ng_1.k(state.og_1);
    if (!equals(tok.ua_1, Or_instance))
      break $l$loop_0;
    state.og_1 = state.og_1 + 1 | 0;
    var right = parseAnd($this, state);
    if (right == null) {
      var tmp0 = state.pg_1;
      // Inline function 'kotlin.collections.plusAssign' call
      var element = new ParseError('Expected expression after `or`.', tok.va_1);
      tmp0.f(element);
      break $l$loop_0;
    }
    left = left == null ? right : combine($this, NodeKind_OR_getInstance(), left, right);
  }
  return left;
}
function parseAnd($this, state) {
  var left = parseNot($this, state);
  $l$loop_0: while (state.og_1 < state.ng_1.j()) {
    var tok = state.ng_1.k(state.og_1);
    if (equals(tok.ua_1, Or_instance) || equals(tok.ua_1, RParen_instance))
      break $l$loop_0;
    if (equals(tok.ua_1, And_instance)) {
      state.og_1 = state.og_1 + 1 | 0;
    }
    var right = parseNot($this, state);
    if (right == null) {
      if (equals(tok.ua_1, And_instance)) {
        var tmp0 = state.pg_1;
        // Inline function 'kotlin.collections.plusAssign' call
        var element = new ParseError('Expected expression after `and`.', tok.va_1);
        tmp0.f(element);
      }
      break $l$loop_0;
    }
    left = left == null ? right : combine($this, NodeKind_AND_getInstance(), left, right);
  }
  return left;
}
function parseNot($this, state) {
  if (state.og_1 >= state.ng_1.j())
    return null;
  var tok = state.ng_1.k(state.og_1);
  if (equals(tok.ua_1, Not_instance)) {
    state.og_1 = state.og_1 + 1 | 0;
    var child = parseAtom($this, state);
    if (child == null) {
      var tmp0 = state.pg_1;
      // Inline function 'kotlin.collections.plusAssign' call
      var element = new ParseError('Expected expression after `not`.', tok.va_1);
      tmp0.f(element);
      return null;
    }
    return new NotNode(child, new Span(tok.va_1.oa_1, child.gb().pa_1));
  }
  return parseAtom($this, state);
}
function parseAtom($this, state) {
  if (state.og_1 >= state.ng_1.j())
    return null;
  var tok = state.ng_1.k(state.og_1);
  if (equals(tok.ua_1, LParen_instance)) {
    state.og_1 = state.og_1 + 1 | 0;
    var inner = parseOr($this, state);
    if (state.og_1 < state.ng_1.j() && equals(state.ng_1.k(state.og_1).ua_1, RParen_instance)) {
      var rparen = state.ng_1.k(state.og_1);
      state.og_1 = state.og_1 + 1 | 0;
      if (inner == null) {
        var tmp0 = state.pg_1;
        // Inline function 'kotlin.collections.plusAssign' call
        var element = new ParseError('Empty group `()`.', new Span(tok.va_1.oa_1, rparen.va_1.pa_1));
        tmp0.f(element);
        return null;
      }
      return withSpan($this, inner, new Span(tok.va_1.oa_1, rparen.va_1.pa_1));
    }
    var tmp0_0 = state.pg_1;
    // Inline function 'kotlin.collections.plusAssign' call
    var element_0 = new ParseError('Unmatched `(`.', tok.va_1);
    tmp0_0.f(element_0);
    return inner;
  }
  if (equals(tok.ua_1, RParen_instance))
    return null;
  if (equals(tok.ua_1, Or_instance) || equals(tok.ua_1, And_instance) || equals(tok.ua_1, Not_instance))
    return null;
  state.og_1 = state.og_1 + 1 | 0;
  var atom = new AtomNode(tok.xa_1, tok.ya_1, tok.za_1, tok.ab_1, tok.bb_1, tok.cb_1, tok.va_1, tok.db_1);
  return tok.wa_1 ? new NotNode(atom, tok.va_1) : atom;
}
function NodeKind(name, ordinal) {
  Enum.call(this, name, ordinal);
}
function combine($this, kind, left, right) {
  var span = new Span(left.gb().oa_1, right.gb().pa_1);
  var children = ArrayList_init_$Create$();
  var tmp;
  if (kind.equals(NodeKind_AND_getInstance())) {
    tmp = left instanceof AndNode;
  } else {
    tmp = false;
  }
  if (tmp) {
    // Inline function 'kotlin.collections.plusAssign' call
    var elements = left.eb_1;
    addAll(children, elements);
  } else {
    var tmp_0;
    if (kind.equals(NodeKind_OR_getInstance())) {
      tmp_0 = left instanceof OrNode;
    } else {
      tmp_0 = false;
    }
    if (tmp_0) {
      // Inline function 'kotlin.collections.plusAssign' call
      var elements_0 = left.jb_1;
      addAll(children, elements_0);
    } else {
      // Inline function 'kotlin.collections.plusAssign' call
      children.f(left);
    }
  }
  var tmp_1;
  if (kind.equals(NodeKind_AND_getInstance())) {
    tmp_1 = right instanceof AndNode;
  } else {
    tmp_1 = false;
  }
  if (tmp_1) {
    // Inline function 'kotlin.collections.plusAssign' call
    var elements_1 = right.eb_1;
    addAll(children, elements_1);
  } else {
    var tmp_2;
    if (kind.equals(NodeKind_OR_getInstance())) {
      tmp_2 = right instanceof OrNode;
    } else {
      tmp_2 = false;
    }
    if (tmp_2) {
      // Inline function 'kotlin.collections.plusAssign' call
      var elements_2 = right.jb_1;
      addAll(children, elements_2);
    } else {
      // Inline function 'kotlin.collections.plusAssign' call
      children.f(right);
    }
  }
  var tmp_3;
  switch (kind.r1_1) {
    case 0:
      tmp_3 = new AndNode(children, span);
      break;
    case 1:
      tmp_3 = new OrNode(children, span);
      break;
    default:
      noWhenBranchMatchedException();
      break;
  }
  return tmp_3;
}
function withSpan($this, $receiver, span) {
  var tmp;
  if ($receiver instanceof AndNode) {
    tmp = $receiver.ib(VOID, span);
  } else {
    if ($receiver instanceof OrNode) {
      tmp = $receiver.lb(VOID, span);
    } else {
      if ($receiver instanceof NotNode) {
        tmp = $receiver.pb(VOID, span);
      } else {
        if ($receiver instanceof AtomNode) {
          tmp = $receiver.zb(VOID, VOID, VOID, VOID, VOID, VOID, span);
        } else {
          noWhenBranchMatchedException();
        }
      }
    }
  }
  return tmp;
}
function NodeKind_AND_getInstance() {
  static_init_0();
  return NodeKind_AND_instance;
}
function NodeKind_OR_getInstance() {
  static_init_0();
  return NodeKind_OR_instance;
}
function Parser() {
}
protoOf(Parser).qg = function (query) {
  var tokens = Lexer_getInstance().ge(query);
  if (tokens.l())
    return new ParseAst(null, emptyList());
  var state = new State(tokens);
  var node = parseOr(this, state);
  if (state.og_1 < tokens.j()) {
    var tok = tokens.k(state.og_1);
    var tmp0 = state.pg_1;
    // Inline function 'kotlin.collections.plusAssign' call
    var element = new ParseError(equals(tok.ua_1, RParen_instance) ? 'Unmatched `)`.' : 'Unexpected token.', tok.va_1);
    tmp0.f(element);
  }
  return new ParseAst(node, state.pg_1);
};
protoOf(Parser).rg = function (query) {
  var _iterator__ex2g4s = Lexer_getInstance().ge(query).g();
  while (_iterator__ex2g4s.h()) {
    var tok = _iterator__ex2g4s.i();
    var tmp0_subject = tok.ua_1;
    if (equals(tmp0_subject, LParen_instance) || equals(tmp0_subject, RParen_instance) || (equals(tmp0_subject, Or_instance) || equals(tmp0_subject, Not_instance)))
      return true;
  }
  return false;
};
var Parser_instance;
function Parser_getInstance() {
  return Parser_instance;
}
function SearchCard() {
}
function SearchView(card) {
  this.bf_1 = card;
  this.cf_1 = null;
  this.df_1 = null;
  this.ef_1 = null;
  this.ff_1 = null;
  this.gf_1 = null;
  this.hf_1 = null;
  this.if_1 = null;
  this.jf_1 = null;
  this.kf_1 = null;
}
protoOf(SearchView).lf = function () {
  var tmp0_elvis_lhs = this.cf_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.collections.map' call
    var this_0 = plus(listOf(this.bf_1.se()), this.bf_1.te());
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
    var _iterator__ex2g4s = this_0.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      // Inline function 'kotlin.text.lowercase' call
      // Inline function 'kotlin.js.asDynamic' call
      var tmp$ret$2 = item.toLowerCase();
      destination.f(tmp$ret$2);
    }
    // Inline function 'kotlin.collections.toTypedArray' call
    // Inline function 'kotlin.also' call
    var this_1 = copyToArray(destination);
    this.cf_1 = this_1;
    tmp = this_1;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).mf = function () {
  var tmp0_elvis_lhs = this.df_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    var tmp1_safe_receiver = this.bf_1.mf();
    var tmp_0;
    if (tmp1_safe_receiver == null) {
      tmp_0 = null;
    } else {
      // Inline function 'kotlin.text.lowercase' call
      // Inline function 'kotlin.js.asDynamic' call
      tmp_0 = tmp1_safe_receiver.toLowerCase();
    }
    var tmp2_elvis_lhs = tmp_0;
    // Inline function 'kotlin.also' call
    var this_0 = tmp2_elvis_lhs == null ? '' : tmp2_elvis_lhs;
    this.df_1 = this_0;
    tmp = this_0;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).nf = function () {
  var tmp0_elvis_lhs = this.ef_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.collections.map' call
    var this_0 = plus(plus(this.bf_1.wf(), this.bf_1.xf()), this.bf_1.ag());
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
    var _iterator__ex2g4s = this_0.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      // Inline function 'kotlin.text.lowercase' call
      // Inline function 'kotlin.js.asDynamic' call
      var tmp$ret$2 = item.toLowerCase();
      destination.f(tmp$ret$2);
    }
    // Inline function 'kotlin.collections.toTypedArray' call
    // Inline function 'kotlin.also' call
    var this_1 = copyToArray(destination);
    this.ef_1 = this_1;
    tmp = this_1;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).uf = function () {
  var tmp0_elvis_lhs = this.ff_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.collections.map' call
    var this_0 = this.bf_1.uf();
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
    var _iterator__ex2g4s = this_0.g();
    while (_iterator__ex2g4s.h()) {
      var item = _iterator__ex2g4s.i();
      // Inline function 'kotlin.text.lowercase' call
      // Inline function 'kotlin.js.asDynamic' call
      var tmp$ret$2 = item.toLowerCase();
      destination.f(tmp$ret$2);
    }
    // Inline function 'kotlin.collections.toTypedArray' call
    // Inline function 'kotlin.also' call
    var this_1 = copyToArray(destination);
    this.ff_1 = this_1;
    tmp = this_1;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).zf = function () {
  var tmp0_elvis_lhs = this.gf_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.collections.toTypedArray' call
    var this_0 = this.bf_1.zf();
    // Inline function 'kotlin.also' call
    var this_1 = copyToArray(this_0);
    this.gf_1 = this_1;
    tmp = this_1;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).cg = function () {
  var tmp0_elvis_lhs = this.hf_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.collections.toTypedArray' call
    var this_0 = this.bf_1.cg();
    // Inline function 'kotlin.also' call
    var this_1 = copyToArray(this_0);
    this.hf_1 = this_1;
    tmp = this_1;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).bg = function () {
  var tmp0_elvis_lhs = this.if_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.collections.toTypedArray' call
    var this_0 = sorted(ManaCostMatch_instance.ke(this.bf_1.sg()));
    // Inline function 'kotlin.also' call
    var this_1 = copyToArray(this_0);
    this.if_1 = this_1;
    tmp = this_1;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).of = function () {
  var tmp0_elvis_lhs = this.jf_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.also' call
    var this_0 = ManaCostMatch_instance.ne(this.bg());
    this.jf_1 = this_0;
    tmp = this_0;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
protoOf(SearchView).pf = function () {
  var tmp0_elvis_lhs = this.kf_1;
  var tmp;
  if (tmp0_elvis_lhs == null) {
    // Inline function 'kotlin.also' call
    var this_0 = ManaCostMatch_instance.me(this.bf_1.zf());
    this.kf_1 = this_0;
    tmp = this_0;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  return tmp;
};
function findSetFilter($this, root) {
  // Inline function 'kotlin.apply' call
  var this_0 = ArrayDeque_init_$Create$();
  this_0.f(root);
  var stack = this_0;
  $l$loop: while (true) {
    // Inline function 'kotlin.collections.isNotEmpty' call
    if (!!stack.l()) {
      break $l$loop;
    }
    var node = stack.r8();
    if (node instanceof AtomNode) {
      if (!node.tb_1 && (node.qb_1 === 's' || node.qb_1 === 'set') && (node.rb_1.equals(Op_EQ_getInstance()) || node.rb_1.equals(Op_EXACT_getInstance()))) {
        // Inline function 'kotlin.text.trim' call
        var this_1 = node.sb_1;
        // Inline function 'kotlin.text.uppercase' call
        // Inline function 'kotlin.js.asDynamic' call
        // Inline function 'kotlin.takeIf' call
        var this_2 = toString_1(trim(isCharSequence(this_1) ? this_1 : THROW_CCE())).toUpperCase();
        var tmp;
        // Inline function 'kotlin.text.isNotEmpty' call
        if (charSequenceLength(this_2) > 0) {
          tmp = this_2;
        } else {
          tmp = null;
        }
        var tmp0_safe_receiver = tmp;
        if (tmp0_safe_receiver == null)
          null;
        else {
          // Inline function 'kotlin.let' call
          return tmp0_safe_receiver;
        }
      }
    } else {
      if (node instanceof AndNode)
        stack.p(node.eb_1);
      else {
        var tmp_0;
        if (node instanceof OrNode) {
          tmp_0 = true;
        } else {
          tmp_0 = node instanceof NotNode;
        }
        if (!tmp_0) {
          noWhenBranchMatchedException();
        }
      }
    }
  }
  return null;
}
function nameConstraint($this, node) {
  var tmp;
  if (node instanceof AtomNode) {
    tmp = isNameAtom($this, node) ? Matchers_getInstance().ze(node).w8_1 : null;
  } else {
    if (node instanceof NotNode) {
      tmp = null;
    } else {
      if (node instanceof AndNode) {
        // Inline function 'kotlin.collections.mapNotNull' call
        var tmp0 = node.eb_1;
        // Inline function 'kotlin.collections.mapNotNullTo' call
        var destination = ArrayList_init_$Create$();
        // Inline function 'kotlin.collections.forEach' call
        var _iterator__ex2g4s = tmp0.g();
        while (_iterator__ex2g4s.h()) {
          var element = _iterator__ex2g4s.i();
          var tmp0_safe_receiver = nameConstraint($this, element);
          if (tmp0_safe_receiver == null)
            null;
          else {
            // Inline function 'kotlin.let' call
            destination.f(tmp0_safe_receiver);
          }
        }
        var tests = destination;
        var tmp_0;
        if (tests.l()) {
          tmp_0 = null;
        } else {
          tmp_0 = SearchQuery$Companion$nameConstraint$lambda(tests);
        }
        tmp = tmp_0;
      } else {
        if (node instanceof OrNode) {
          // Inline function 'kotlin.collections.map' call
          var this_0 = node.jb_1;
          // Inline function 'kotlin.collections.mapTo' call
          var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
          var _iterator__ex2g4s_0 = this_0.g();
          while (_iterator__ex2g4s_0.h()) {
            var item = _iterator__ex2g4s_0.i();
            var tmp$ret$9 = nameConstraint($this, item);
            destination_0.f(tmp$ret$9);
          }
          var tests_0 = destination_0;
          var tmp_1;
          var tmp$ret$10;
          $l$block_0: {
            // Inline function 'kotlin.collections.any' call
            var tmp_2;
            if (isInterface(tests_0, Collection)) {
              tmp_2 = tests_0.l();
            } else {
              tmp_2 = false;
            }
            if (tmp_2) {
              tmp$ret$10 = false;
              break $l$block_0;
            }
            var _iterator__ex2g4s_1 = tests_0.g();
            while (_iterator__ex2g4s_1.h()) {
              var element_0 = _iterator__ex2g4s_1.i();
              if (element_0 == null) {
                tmp$ret$10 = true;
                break $l$block_0;
              }
            }
            tmp$ret$10 = false;
          }
          if (tmp$ret$10) {
            tmp_1 = null;
          } else {
            tmp_1 = SearchQuery$Companion$nameConstraint$lambda_0(tests_0);
          }
          tmp = tmp_1;
        } else {
          noWhenBranchMatchedException();
        }
      }
    }
  }
  return tmp;
}
function isNameAtom($this, atom) {
  return atom.qb_1 == null || ((atom.qb_1 === 'name' || atom.qb_1 === 'n') && (atom.rb_1.equals(Op_EQ_getInstance()) || atom.rb_1.equals(Op_EXACT_getInstance())));
}
function explain($this, node, parent) {
  var tmp;
  if (node instanceof AtomNode) {
    var tmp_0 = Matchers_getInstance().md_1;
    var tmp1_elvis_lhs = node.qb_1;
    tmp = getValue(tmp_0, tmp1_elvis_lhs == null ? 'name' : tmp1_elvis_lhs).kg(node);
  } else {
    if (node instanceof NotNode) {
      tmp = 'not (' + explain($this, node.mb_1, node) + ')';
    } else {
      if (node instanceof AndNode) {
        // Inline function 'kotlin.let' call
        var it = joinToString_0(node.eb_1, ' and ', VOID, VOID, VOID, VOID, SearchQuery$Companion$explain$lambda(node));
        var tmp_1;
        var tmp_2;
        if (parent instanceof AndNode) {
          tmp_2 = true;
        } else {
          tmp_2 = parent instanceof OrNode;
        }
        if (tmp_2) {
          tmp_1 = '(' + it + ')';
        } else {
          tmp_1 = it;
        }
        tmp = tmp_1;
      } else {
        if (node instanceof OrNode) {
          // Inline function 'kotlin.let' call
          var it_0 = joinToString_0(node.jb_1, ' or ', VOID, VOID, VOID, VOID, SearchQuery$Companion$explain$lambda_0(node));
          var tmp_3;
          var tmp_4;
          if (parent instanceof AndNode) {
            tmp_4 = true;
          } else {
            tmp_4 = parent instanceof OrNode;
          }
          if (tmp_4) {
            tmp_3 = '(' + it_0 + ')';
          } else {
            tmp_3 = it_0;
          }
          tmp = tmp_3;
        } else {
          noWhenBranchMatchedException();
        }
      }
    }
  }
  return tmp;
}
function SearchQuery$Companion$EMPTY$lambda(it) {
  return true;
}
function SearchQuery$Companion$nameConstraint$lambda($tests) {
  return function (s) {
    var tmp0 = $tests;
    var tmp$ret$0;
    $l$block_0: {
      // Inline function 'kotlin.collections.all' call
      var tmp;
      if (isInterface(tmp0, Collection)) {
        tmp = tmp0.l();
      } else {
        tmp = false;
      }
      if (tmp) {
        tmp$ret$0 = true;
        break $l$block_0;
      }
      var _iterator__ex2g4s = tmp0.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if (!element(s)) {
          tmp$ret$0 = false;
          break $l$block_0;
        }
      }
      tmp$ret$0 = true;
    }
    return tmp$ret$0;
  };
}
function SearchQuery$Companion$nameConstraint$lambda_0($tests) {
  return function (s) {
    var tmp0 = $tests;
    var tmp$ret$0;
    $l$block_0: {
      // Inline function 'kotlin.collections.any' call
      var tmp;
      if (isInterface(tmp0, Collection)) {
        tmp = tmp0.l();
      } else {
        tmp = false;
      }
      if (tmp) {
        tmp$ret$0 = false;
        break $l$block_0;
      }
      var _iterator__ex2g4s = tmp0.g();
      while (_iterator__ex2g4s.h()) {
        var element = _iterator__ex2g4s.i();
        if (ensureNotNull(element)(s)) {
          tmp$ret$0 = true;
          break $l$block_0;
        }
      }
      tmp$ret$0 = false;
    }
    return tmp$ret$0;
  };
}
function SearchQuery$Companion$explain$lambda($node) {
  return function (it) {
    return explain(Companion_getInstance_12(), it, $node);
  };
}
function SearchQuery$Companion$explain$lambda_0($node) {
  return function (it) {
    return explain(Companion_getInstance_12(), it, $node);
  };
}
function _get_nameTest__qak30y($this) {
  var tmp0 = $this.ah_1;
  var tmp = KProperty1;
  // Inline function 'kotlin.getValue' call
  getPropertyCallableRef('nameTest', 1, tmp, SearchQuery$_get_nameTest_$ref_ipm13z(), null);
  return tmp0.k1();
}
function Companion_12() {
  Companion_instance_12 = this;
  var tmp = this;
  var tmp_0 = emptyList();
  tmp.bh_1 = new SearchQuery('', null, tmp_0, SearchQuery$Companion$EMPTY$lambda);
}
protoOf(Companion_12).ch = function (query) {
  if (isBlank(query))
    return this.bh_1;
  var parsed = Parser_instance.qg(query);
  var compiled = Evaluator_getInstance().wd(parsed.lg_1);
  return new SearchQuery(query, parsed.lg_1, plus(parsed.mg_1, compiled.oc_1), compiled.nc_1);
};
var Companion_instance_12;
function Companion_getInstance_12() {
  if (Companion_instance_12 == null)
    new Companion_12();
  return Companion_instance_12;
}
function SearchQuery$isAdvanced$delegate$lambda(this$0) {
  return function () {
    return Parser_instance.rg(this$0.tg_1);
  };
}
function SearchQuery$_get_isAdvanced_$ref_tdfzxq() {
  return constructCallableReference(function (p0) {
    return p0.dh();
  }, 1, 0, 0);
}
function SearchQuery$setFilter$delegate$lambda(this$0) {
  return function () {
    var tmp0_safe_receiver = this$0.ug_1;
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      var this_0 = Companion_getInstance_12();
      tmp = findSetFilter(this_0, tmp0_safe_receiver);
    }
    return tmp;
  };
}
function SearchQuery$_get_setFilter_$ref_6uppvi() {
  return constructCallableReference(function (p0) {
    return p0.eh();
  }, 1, 0, 1);
}
function SearchQuery$explanation$delegate$lambda(this$0) {
  return function () {
    var tmp;
    var tmp_0;
    if (this$0.ug_1 == null) {
      tmp_0 = true;
    } else {
      // Inline function 'kotlin.collections.isNotEmpty' call
      tmp_0 = !this$0.vg_1.l();
    }
    if (tmp_0) {
      tmp = null;
    } else {
      tmp = explain(Companion_getInstance_12(), this$0.ug_1, null);
    }
    return tmp;
  };
}
function SearchQuery$_get_explanation_$ref_5eojzx() {
  return constructCallableReference(function (p0) {
    return p0.fh();
  }, 1, 0, 2);
}
function SearchQuery$nameTest$delegate$lambda(this$0) {
  return function () {
    var tmp0_safe_receiver = this$0.ug_1;
    var tmp;
    if (tmp0_safe_receiver == null) {
      tmp = null;
    } else {
      // Inline function 'kotlin.let' call
      var this_0 = Companion_getInstance_12();
      tmp = nameConstraint(this_0, tmp0_safe_receiver);
    }
    return tmp;
  };
}
function SearchQuery$_get_nameTest_$ref_ipm13z() {
  return constructCallableReference(function (p0) {
    return _get_nameTest__qak30y(p0);
  }, 1, 0, 3);
}
function SearchQuery(text, ast, errors, predicate) {
  Companion_getInstance_12();
  this.tg_1 = text;
  this.ug_1 = ast;
  this.vg_1 = errors;
  this.wg_1 = predicate;
  var tmp = this;
  tmp.xg_1 = lazy(SearchQuery$isAdvanced$delegate$lambda(this));
  var tmp_0 = this;
  tmp_0.yg_1 = lazy(SearchQuery$setFilter$delegate$lambda(this));
  var tmp_1 = this;
  tmp_1.zg_1 = lazy(SearchQuery$explanation$delegate$lambda(this));
  var tmp_2 = this;
  tmp_2.ah_1 = lazy(SearchQuery$nameTest$delegate$lambda(this));
}
protoOf(SearchQuery).gh = function (card) {
  return this.wg_1(card);
};
protoOf(SearchQuery).hh = function () {
  return this.ug_1 == null;
};
protoOf(SearchQuery).dh = function () {
  var tmp0 = this.xg_1;
  var tmp = KProperty1;
  // Inline function 'kotlin.getValue' call
  getPropertyCallableRef('isAdvanced', 1, tmp, SearchQuery$_get_isAdvanced_$ref_tdfzxq(), null);
  return tmp0.k1();
};
protoOf(SearchQuery).eh = function () {
  var tmp0 = this.yg_1;
  var tmp = KProperty1;
  // Inline function 'kotlin.getValue' call
  getPropertyCallableRef('setFilter', 1, tmp, SearchQuery$_get_setFilter_$ref_6uppvi(), null);
  return tmp0.k1();
};
protoOf(SearchQuery).fh = function () {
  var tmp0 = this.zg_1;
  var tmp = KProperty1;
  // Inline function 'kotlin.getValue' call
  getPropertyCallableRef('explanation', 1, tmp, SearchQuery$_get_explanation_$ref_5eojzx(), null);
  return tmp0.k1();
};
protoOf(SearchQuery).ih = function () {
  var root = this.ug_1;
  var tmp;
  if (root == null) {
    tmp = emptyList();
  } else {
    if (root instanceof AndNode) {
      // Inline function 'kotlin.collections.map' call
      var this_0 = root.eb_1;
      // Inline function 'kotlin.collections.mapTo' call
      var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
      var _iterator__ex2g4s = this_0.g();
      while (_iterator__ex2g4s.h()) {
        var item = _iterator__ex2g4s.i();
        var tmp$ret$2 = item.gb();
        destination.f(tmp$ret$2);
      }
      tmp = destination;
    } else {
      if (root instanceof OrNode) {
        tmp = emptyList();
      } else {
        tmp = listOf(root.gb());
      }
    }
  }
  return tmp;
};
protoOf(SearchQuery).jh = function () {
  var tmp = this.ug_1;
  return !(tmp instanceof OrNode);
};
protoOf(SearchQuery).kh = function () {
  return !(_get_nameTest__qak30y(this) == null);
};
protoOf(SearchQuery).lh = function (card) {
  var tmp0_elvis_lhs = _get_nameTest__qak30y(this);
  var tmp;
  if (tmp0_elvis_lhs == null) {
    return null;
  } else {
    tmp = tmp0_elvis_lhs;
  }
  var test = tmp;
  if (card.te().l() || test(card.se()))
    return null;
  var tmp0 = card.te();
  var tmp$ret$0;
  $l$block: {
    // Inline function 'kotlin.collections.firstOrNull' call
    var _iterator__ex2g4s = tmp0.g();
    while (_iterator__ex2g4s.h()) {
      var element = _iterator__ex2g4s.i();
      if (test(element)) {
        tmp$ret$0 = element;
        break $l$block;
      }
    }
    tmp$ret$0 = null;
  }
  return tmp$ret$0;
};
function compileQueryRegex(pattern, flags) {
  var tmp;
  try {
    tmp = new RegExp(pattern, flags);
  } catch ($p) {
    var tmp_0;
    if ($p instanceof Error) {
      var e = $p;
      var tmp0_elvis_lhs = e.message;
      throw IllegalArgumentException_init_$Create$_1(tmp0_elvis_lhs == null ? 'invalid pattern' : tmp0_elvis_lhs, e);
    } else {
      throw $p;
    }
  }
  var compiled = tmp;
  return compileQueryRegex$lambda(compiled);
}
function containsText(_this__u8e3s4, needle) {
  // Inline function 'kotlin.js.asDynamic' call
  // Inline function 'kotlin.js.unsafeCast' call
  return _this__u8e3s4.indexOf(needle) >= 0;
}
function compileQueryRegex$lambda($compiled) {
  return function (input) {
    // Inline function 'kotlin.js.unsafeCast' call
    return $compiled.test(input);
  };
}
function get_adapters() {
  _init_properties_WebSearch_kt__m35l1y();
  return adapters;
}
var adapters;
function compileQuery(query) {
  _init_properties_WebSearch_kt__m35l1y();
  return new CompiledQuery(Companion_getInstance_12().ch(query));
}
function isAdvancedQuery(query) {
  _init_properties_WebSearch_kt__m35l1y();
  return Parser_instance.rg(query);
}
function searchKeys() {
  _init_properties_WebSearch_kt__m35l1y();
  // Inline function 'kotlin.collections.map' call
  var this_0 = distinct(Matchers_getInstance().md_1.o1());
  // Inline function 'kotlin.collections.mapTo' call
  var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
  var _iterator__ex2g4s = this_0.g();
  while (_iterator__ex2g4s.h()) {
    var item = _iterator__ex2g4s.i();
    // Inline function 'kotlin.collections.toTypedArray' call
    var this_1 = item.ig();
    var tmp = copyToArray(this_1);
    // Inline function 'kotlin.collections.map' call
    var this_2 = item.rd();
    // Inline function 'kotlin.collections.mapTo' call
    var destination_0 = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_2, 10));
    var _iterator__ex2g4s_0 = this_2.g();
    while (_iterator__ex2g4s_0.h()) {
      var item_0 = _iterator__ex2g4s_0.i();
      var tmp$ret$6 = item_0.ta_1;
      destination_0.f(tmp$ret$6);
    }
    // Inline function 'kotlin.collections.toTypedArray' call
    var tmp$ret$7 = copyToArray(destination_0);
    var tmp$ret$2 = new SearchKey(tmp, tmp$ret$7, item.jg());
    destination.f(tmp$ret$2);
  }
  // Inline function 'kotlin.collections.toTypedArray' call
  return copyToArray(destination);
}
function SearchKey(aliases, ops, description) {
  this.aliases = aliases;
  this.ops = ops;
  this.description = description;
}
protoOf(SearchKey).ig = function () {
  return this.aliases;
};
protoOf(SearchKey).rd = function () {
  return this.ops;
};
protoOf(SearchKey).jg = function () {
  return this.description;
};
function CompiledQuery(query) {
  this.mh_1 = query;
  var tmp = this;
  // Inline function 'kotlin.collections.map' call
  var this_0 = this.mh_1.vg_1;
  // Inline function 'kotlin.collections.mapTo' call
  var destination = ArrayList_init_$Create$_0(collectionSizeOrDefault(this_0, 10));
  var _iterator__ex2g4s = this_0.g();
  while (_iterator__ex2g4s.h()) {
    var item = _iterator__ex2g4s.i();
    var tmp0_safe_receiver = item.dc_1;
    var tmp_0;
    if (tmp0_safe_receiver == null) {
      tmp_0 = null;
    } else {
      // Inline function 'kotlin.let' call
      tmp_0 = new QueryFix(tmp0_safe_receiver.ec_1.oa_1, tmp0_safe_receiver.ec_1.pa_1, tmp0_safe_receiver.fc_1);
    }
    var tmp$ret$2 = new QueryError(item.ac_1, item.bc_1.oa_1, item.bc_1.pa_1, item.cc_1, tmp_0);
    destination.f(tmp$ret$2);
  }
  // Inline function 'kotlin.collections.toTypedArray' call
  tmp.errors = copyToArray(destination);
}
protoOf(CompiledQuery).nh = function () {
  return this.errors;
};
protoOf(CompiledQuery).hh = function () {
  return this.mh_1.hh();
};
protoOf(CompiledQuery).dh = function () {
  return this.mh_1.dh();
};
protoOf(CompiledQuery).eh = function () {
  return this.mh_1.eh();
};
protoOf(CompiledQuery).fh = function () {
  return this.mh_1.fh();
};
protoOf(CompiledQuery).kh = function () {
  return this.mh_1.kh();
};
protoOf(CompiledQuery).jh = function () {
  return this.mh_1.jh();
};
protoOf(CompiledQuery).oh = function () {
  // Inline function 'kotlin.collections.flatMap' call
  var tmp0 = this.mh_1.ih();
  // Inline function 'kotlin.collections.flatMapTo' call
  var destination = ArrayList_init_$Create$();
  var _iterator__ex2g4s = tmp0.g();
  while (_iterator__ex2g4s.h()) {
    var element = _iterator__ex2g4s.i();
    var list = listOf_0([element.oa_1, element.pa_1]);
    addAll(destination, list);
  }
  return toIntArray(destination);
};
protoOf(CompiledQuery).matches = function (card) {
  return this.mh_1.gh(adapt(card));
};
protoOf(CompiledQuery).matchedPrintedName = function (card) {
  return this.mh_1.lh(adapt(card));
};
function QueryError(message, start, end, suggestion, fix) {
  this.message = message;
  this.start = start;
  this.end = end;
  this.suggestion = suggestion;
  this.fix = fix;
}
protoOf(QueryError).f6 = function () {
  return this.message;
};
protoOf(QueryError).j7 = function () {
  return this.start;
};
protoOf(QueryError).ph = function () {
  return this.end;
};
protoOf(QueryError).qh = function () {
  return this.suggestion;
};
protoOf(QueryError).rh = function () {
  return this.fix;
};
function QueryFix(start, end, text) {
  this.start = start;
  this.end = end;
  this.text = text;
}
protoOf(QueryFix).j7 = function () {
  return this.start;
};
protoOf(QueryFix).ph = function () {
  return this.end;
};
protoOf(QueryFix).sh = function () {
  return this.text;
};
function adapt(card) {
  _init_properties_WebSearch_kt__m35l1y();
  var cached = get_adapters().get(card);
  if (cached != null) {
    // Inline function 'kotlin.js.unsafeCast' call
    return cached;
  }
  var adapted = new JsSearchCard(card);
  get_adapters().set(card, adapted);
  return adapted;
}
function JsSearchCard(o) {
  var tmp = this;
  // Inline function 'kotlin.js.unsafeCast' call
  tmp.th_1 = o.name;
  var tmp_0 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  var tmp0_elvis_lhs = o.manaCost;
  tmp_0.uh_1 = tmp0_elvis_lhs == null ? '' : tmp0_elvis_lhs;
  var tmp_1 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  var tmp0_elvis_lhs_0 = o.cmc;
  tmp_1.vh_1 = tmp0_elvis_lhs_0 == null ? 0 : tmp0_elvis_lhs_0;
  this.wh_1 = strings(o.colors);
  this.xh_1 = strings(o.colorIdentity);
  this.yh_1 = strings(o.cardTypes);
  this.zh_1 = strings(o.supertypes);
  this.ai_1 = strings(o.subtypes);
  this.bi_1 = o.basicLand == true;
  var tmp_2 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  var tmp0_elvis_lhs_1 = o.rarity;
  tmp_2.ci_1 = tmp0_elvis_lhs_1 == null ? '' : tmp0_elvis_lhs_1;
  var tmp_3 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  tmp_3.di_1 = o.setCode;
  var tmp_4 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  tmp_4.ei_1 = o.collectorNumber;
  var tmp_5 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  tmp_5.fi_1 = o.oracleText;
  var tmp_6 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  tmp_6.gi_1 = o.power;
  var tmp_7 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  tmp_7.hi_1 = o.toughness;
  var tmp_8 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  tmp_8.ii_1 = o.loyalty;
  this.ji_1 = strings(o.keywords);
  this.ki_1 = strings(o.legalFormats);
  this.li_1 = o.isDoubleFaced == true;
  this.mi_1 = strings(o.printingSetCodes);
  var tmp_9 = this;
  // Inline function 'kotlin.js.unsafeCast' call
  var tmp0_safe_receiver = o.printedNamePrintings;
  var tmp_10;
  if (tmp0_safe_receiver == null) {
    tmp_10 = null;
  } else {
    // Inline function 'kotlin.collections.map' call
    // Inline function 'kotlin.collections.mapTo' call
    var destination = ArrayList_init_$Create$_0(tmp0_safe_receiver.length);
    var inductionVariable = 0;
    var last = tmp0_safe_receiver.length;
    while (inductionVariable < last) {
      var item = tmp0_safe_receiver[inductionVariable];
      inductionVariable = inductionVariable + 1 | 0;
      // Inline function 'kotlin.js.unsafeCast' call
      var tmp$ret$13 = item.name;
      destination.f(tmp$ret$13);
    }
    tmp_10 = destination;
  }
  var tmp1_elvis_lhs = tmp_10;
  tmp_9.ni_1 = tmp1_elvis_lhs == null ? emptyList() : tmp1_elvis_lhs;
  this.oi_1 = new SearchView(this);
}
protoOf(JsSearchCard).se = function () {
  return this.th_1;
};
protoOf(JsSearchCard).sg = function () {
  return this.uh_1;
};
protoOf(JsSearchCard).ue = function () {
  return this.vh_1;
};
protoOf(JsSearchCard).cg = function () {
  return this.wh_1;
};
protoOf(JsSearchCard).zf = function () {
  return this.xh_1;
};
protoOf(JsSearchCard).wf = function () {
  return this.yh_1;
};
protoOf(JsSearchCard).xf = function () {
  return this.zh_1;
};
protoOf(JsSearchCard).ag = function () {
  return this.ai_1;
};
protoOf(JsSearchCard).yf = function () {
  return this.bi_1;
};
protoOf(JsSearchCard).qf = function () {
  return this.ci_1;
};
protoOf(JsSearchCard).rf = function () {
  return this.di_1;
};
protoOf(JsSearchCard).mf = function () {
  return this.fi_1;
};
protoOf(JsSearchCard).ve = function () {
  return this.gi_1;
};
protoOf(JsSearchCard).xe = function () {
  return this.hi_1;
};
protoOf(JsSearchCard).ye = function () {
  return this.ii_1;
};
protoOf(JsSearchCard).uf = function () {
  return this.ji_1;
};
protoOf(JsSearchCard).tf = function () {
  return this.ki_1;
};
protoOf(JsSearchCard).vf = function () {
  return this.li_1;
};
protoOf(JsSearchCard).sf = function () {
  return this.mi_1;
};
protoOf(JsSearchCard).te = function () {
  return this.ni_1;
};
protoOf(JsSearchCard).af = function () {
  return this.oi_1;
};
function strings(value) {
  _init_properties_WebSearch_kt__m35l1y();
  // Inline function 'kotlin.js.unsafeCast' call
  var tmp1_elvis_lhs = value == null ? null : asList(value);
  return tmp1_elvis_lhs == null ? emptyList() : tmp1_elvis_lhs;
}
var properties_initialized_WebSearch_kt_dfwgj8;
function _init_properties_WebSearch_kt__m35l1y() {
  if (!properties_initialized_WebSearch_kt_dfwgj8) {
    properties_initialized_WebSearch_kt_dfwgj8 = true;
    adapters = new WeakMap();
  }
}
//region block: post-declaration
protoOf(InternalHashMap).z3 = containsAllEntries;
defineProp(protoOf(CompiledQuery), 'isEmpty', protoOf(CompiledQuery).hh);
defineProp(protoOf(CompiledQuery), 'isAdvanced', protoOf(CompiledQuery).dh);
defineProp(protoOf(CompiledQuery), 'setFilter', protoOf(CompiledQuery).eh);
defineProp(protoOf(CompiledQuery), 'explanation', protoOf(CompiledQuery).fh);
defineProp(protoOf(CompiledQuery), 'constrainsName', protoOf(CompiledQuery).kh);
defineProp(protoOf(CompiledQuery), 'isConjunction', protoOf(CompiledQuery).jh);
defineProp(protoOf(CompiledQuery), 'topLevelTermSpans', protoOf(CompiledQuery).oh);
//endregion
//region block: init
Companion_instance_0 = new Companion_0();
Unit_instance = new Unit();
_stableSortingIsSupported = null;
Companion_instance_3 = new Companion_3();
Companion_instance_5 = new Companion_5();
Companion_instance_6 = new Companion_6();
Companion_instance_7 = new Companion_7();
EmptyIterator_instance = new EmptyIterator();
EmptySequence_instance = new EmptySequence();
NaturalOrderComparator_instance = new NaturalOrderComparator();
Companion_instance_10 = new Companion_10();
UNINITIALIZED_VALUE_instance = new UNINITIALIZED_VALUE();
LParen_instance = new LParen();
RParen_instance = new RParen();
Or_instance = new Or();
And_instance = new And();
Not_instance = new Not();
Term_instance = new Term();
Colorless_instance = new Colorless();
Multi_instance = new Multi();
Mono_instance = new Mono();
ManaCostMatch_instance = new ManaCostMatch();
Parser_instance = new Parser();
//endregion
//region block: exports
export {
  compileQuery as compileQuery,
  isAdvancedQuery as isAdvancedQuery,
  searchKeys as searchKeys,
  SearchKey as SearchKey,
  CompiledQuery as CompiledQuery,
  QueryError as QueryError,
  QueryFix as QueryFix,
};
//endregion
