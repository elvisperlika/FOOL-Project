push 0
push 10
push 5
lfp
push -2
add
lw
lfp
push -3
add
lw
bleq label4
push 0
b label5
label4:
push 1
label5:
push 0
beq label2
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