# schemes optimization four
import random


class Randomint4:  # 对象
    def __init__(self, count=10, start=1, stop=100):
        self._count = count
        self.start = start
        self.stop = stop

    def _generate(self):
        # 生成器,无限生成，要多少不在这里处理。
        while True:
            yield random.randint(self.start, self.stop)

    def generate(self):
        # 这里控制取值次数。
        return [next(self._generate()) for _ in range(self._count)]


ri = Randomint4(5, 15, 55)  # 可以传参控制输出个数和范围。
print(ri.generate())