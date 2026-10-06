import re
from pathlib import Path
p = Path(r"c:\Users\Wesley Pecoraro\Desktop\SENAI 2025\SESI\2-IDS\Projetos TCC\feededuc_v9\feededuc\src\main\java\com\projeto\feededuc\frontEnd\HTML\index.html")
s = p.read_text(encoding='utf8')
# extract script blocks
scripts = re.findall(r"<script[^>]*>([\s\S]*?)</script>", s, re.IGNORECASE)
all_js = "\n".join(scripts)
print('Total <script> blocks found:', len(scripts))
print('Total JS size:', len(all_js), 'chars')
# counters
pairs = {'(':')','{':'}','[':']','`':'`'}
stack = []
line_no = 1
last_char_pos = None
for i,ch in enumerate(all_js):
    if ch == '\n':
        line_no += 1
    if ch in '({[`':
        stack.append((ch,line_no,i))
    elif ch in ')}]`':
        if not stack:
            print('Unmatched closing', ch, 'at line', line_no)
            last_char_pos = (ch,line_no,i)
            break
        top, top_line, top_i = stack[-1]
        expected = pairs.get(top)
        if ch == expected:
            stack.pop()
        else:
            print('MISMATCH: top', top, 'at line', top_line, 'but found', ch, 'at line', line_no)
            last_char_pos = (ch,line_no,i)
            break

if stack:
    print('Unmatched openings remain:', len(stack))
    for ch,ln,pos in stack[-10:]:
        print('  open',ch,'at line',ln)
else:
    print('All pairs balanced (according to simple checker)')

# also output last 400 chars for manual inspection
print('\n--- tail of JS (last 400 chars) ---\n')
print(all_js[-400:])
