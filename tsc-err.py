import os, sys

def get_error(l):
    i=l.find('error')
    if (i == -1):
        return "UNKNOWN"
    j=l[i+5:].find(':')
    if (j == -1):
        return "UNKNOWN"
    return l[i+5:i+5+j].strip()
def get_line(l):
    i=l.find('(')
    if (i == -1):
        return "UNKNOWN"
    j=l[i+1:].find(',')
    if (j == -1):
        return "UNKNOWN"
    return l[i+1:i+1+j].strip()
def get_message(l):
    i=l.find('error')
    if (i==-1):
        return "UNKNOWN"
    j=l[i:].find(':')
    if (j==-1):
        return "UNKNOWN"
    return l[i+j:].strip()
    
def get_attr(l):
    res = {}
    res['error'] = get_error(l)
    res['line'] = get_line(l)
    res['message'] = get_message(l)
    return res

def print_info():
    f=open(sys.argv[1])
    s=f.read()
    f.close()
    msgs = s.split('.ts')
    lines = set()
    errors = set()
    err2msg = {}
    for l in msgs[1:]:
        attrs = get_attr(l)
        #print(l)
        #print(attrs)
        lines.add(attrs['line'])
        errors.add(attrs['error'])
        err2msg[attrs['error']]=attrs['message']
    print(lines)
    print(errors)
    print(err2msg)

def get_lines(s):
    msgs = s.split('.ts')
    return msgs[1:]
    
def cmp2():
    s1=open(sys.argv[1]).read()
    s2=open(sys.argv[2]).read()
    msgs1= get_lines(s1)
    msgs2 = get_lines(s2)
    e1=set()
    e2=set()
    ls1=set()
    ls2=set()
    l_m1 = {}
    l_m2 = {}
    for l in msgs1:
        attrs=get_attr(l)
        e1.add(attrs['error'])
        ls1.add(attrs['line'])
        l_m1[attrs['line']]=l #attrs['message']
    for l in msgs2:
        attrs=get_attr(l)
        e2.add(attrs['error'])
        ls2.add(attrs['line'])
        l_m2[attrs['line']]=l #attrs['message']
    #print(e1)
    print(len(ls1))
    #print(e2)
    print(len(ls2))

    
    print(len(ls1.intersection(ls2)))
    
    #print(ls1.difference(ls2))
    d = ls1.difference(ls2)
    for n in d:
        print(l_m1[n])
        print("")
    #print(ls2.difference(ls1))
    print('=========')
    d = ls2.difference(ls1)
    for n in d:
        print(l_m2[n])
        print("")

def cmp3():
    s1=open(sys.argv[1]).read()
    s2=open(sys.argv[2]).read()
    s3=open(sys.argv[3]).read()
    msgs1= get_lines(s1)
    msgs2 = get_lines(s2)
    msgs3 = get_lines(s3)
    e1=set()
    e2=set()
    e3=set()
    ls1=set()
    ls2=set()
    ls3=set()
    l_m1 = {}
    l_m2 = {}
    l_m3 = {}
    for l in msgs1:
        attrs=get_attr(l)
        e1.add(attrs['error'])
        ls1.add(attrs['line'])
        l_m1[attrs['line']]=l #attrs['message']
    for l in msgs2:
        attrs=get_attr(l)
        e2.add(attrs['error'])
        ls2.add(attrs['line'])
        l_m2[attrs['line']]=l #attrs['message']
    for l in msgs3:
        attrs=get_attr(l)
        e3.add(attrs['error'])
        ls3.add(attrs['line'])
        l_m3[attrs['line']]=l #attrs['message']
        
    #print(e1)
    print(len(ls1))
    #print(e2)
    print(len(ls2))
    print(len(ls3))

    
    print(len(ls1.intersection(ls2)))
    print(len(ls2.intersection(ls1)))
    print(len(ls1.intersection(ls3)))
    
    print('diff ' + sys.argv[1] + ' ' + sys.argv[2])
    d = ls1.difference(ls2)
    for n in d:
        print(l_m1[n])
        print("")
    print('diff ' + sys.argv[2] + ' ' + sys.argv[1])
    print('=========')
    d = ls2.difference(ls1)
    for n in d:
        print(l_m2[n])
        print("")

    print('=========')
    print('diff ' + sys.argv[1] + ' ' + sys.argv[3])
    d = ls1.difference(ls3)
    for n in d:
        print(l_m1[n])
        print("")

if __name__ == '__main__':
    #print_lines_errors()
    cmp3()
   
