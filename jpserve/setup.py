import os
from setuptools import setup

def read(fname):
    return open(os.path.join(os.path.dirname(__file__), fname)).read()

setup(
    name = "jpserve",
    version = "0.1.0",
    author = "John Huang",
    author_email = "john.h.cn@gmail.com",
    description = ("jpserve: Calling Python from JAVA"),
    license = "MIT",
    keywords = "jpserve, calling Python from Java",
    url = "https://github.com/johnhuang-cn/jpserve",
    packages=['jpserve'],
    long_description=read('README.txt'),
    classifiers=[
        "Development Status :: 4 - Beta",
        "Topic :: Utilities",
        "Topic :: Software Development",
        "License :: OSI Approved :: MIT License",
        "Programming Language :: Java",
        "Programming Language :: Python :: 3",
    ],
)