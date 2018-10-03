import java_lang_utils as jlu
import tensorflow as tf
from javalang import tokenizer
from functools import reduce

def vectorize_file(path):
    spaces, tokens, tokens_by_line = jlu.tokenize_with_white_space(path)
    # print("\n".join([str(token) for token in tokens]))


    def create_and_append_identifier_references(d, x):
        if isinstance(x, tokenizer.Identifier):
            d.setdefault(x.value, []).append(x)
        return d
    identifier_references = reduce( create_and_append_identifier_references, tokens, {})
    identifier_count = {key:len(array) for key, array in identifier_references.items()}
    # print(identifier_count)
    # print(sum(identifier_count.values())/len(identifier_count))

    def create_abstracter(identifier_count, threshold):
        def get_value(token):
            if isinstance(token, tokenizer.Comment):
                return token.__class__.__name__
            if isinstance(token, tokenizer.Literal):
                return token.__class__.__name__
            if isinstance(token, tokenizer.Identifier):
                if identifier_count[token.value] > threshold:
                    return token.value
                else:
                    return token.__class__.__name__
            if isinstance(token, tokenizer.Operator):
                if token.is_infix():
                    return "InfixOperator"
                if token.is_prefix():
                    return "PrefixOperator"
                if token.is_postfix():
                    return "PostfixOperator"
                if token.is_assignment():
                    return "AssignmentOperator"
            return token.value
        return get_value

    get_value = create_abstracter(identifier_count, 10)

    # print("\n".join( " ".join([get_value(token) for token in line]) for line in tokens_by_line.values()))

    tokens_set = set([get_value(token) for token in tokens])
    set_to_map = lambda set_to_map : { item:i for item, i in zip(set_to_map, range(len(set_to_map))) }
    tokens_map = set_to_map(tokens_set)
    # print(tokens_map)
    spaces_set = set(spaces)
    spaces_map = set_to_map(spaces_set)
    print(spaces_map)

if __name__ == "__main__":
    vectorize_file("./investigations/PlatformModule.java")
