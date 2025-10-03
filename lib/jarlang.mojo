##################################
### TOKENS OF JARLANG LANGUAGE ###
##################################

TT_INT        = "int"
TT_FLOAT      = "float"
TT_STRING     = "chant"
TT_PLUS       = "commune"
TT_MINUS      = "banish"
TT_MUL        = "rally"
TT_DIV        = "slash"      
TT_LPAREN     = "gather"
TT_RPAREN     = "disperse"
TT_EOF        = "end"
TT_RETURN     = "mend"
TT_FUNCTION   = "forge"
TT_IDENTIFIER = "mark"
TT_COMMENT    = "//"
TT_MLSTART    = "guard/"
TT_MLEND      = "/guard"


struct Token:
    var type: String
    var value: String
    
    fn __init__(inout self, type_: String, value: String):
        self.type = type_
        self.value = value

    fn __repr__(self) -> String:
        if self.value:
            return self.type + ":" + self.value
        return self.type

    

