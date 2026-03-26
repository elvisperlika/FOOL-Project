push 0
push 9
push 5
lfp
push -3
add
lw
lfp
push -2
add
lw
bleq label2
push 0
b label3
label2:
push 1
label3:
push 1
beq label0
lfp
push -3
add
lw
print
b label1
label0:
lfp
push -2
add
lw
print
label1:
halt