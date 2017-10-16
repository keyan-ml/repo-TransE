#include<map>
#include<iostream>

using namespace std;

int main()
{
    map<int,double> left_num;
    // 键值对
    map<int, map<int, int>> left_entity;

    left_entity[0][0]++;
    left_entity[0][1]++;

    for (int i = 0; i < 1; i++) {
    	double sum1 = 0, sum2 = 0;
    	for (map<int,int>::iterator it = left_entity[i].begin(); it != left_entity[i].end(); it++) {
    		sum1++;
    		sum2 += it -> second;

            cout << sum2 << endl;
    	}
    	left_num[i] = sum2 / sum1; //计算在关系i下，训练集中左实体的平均id
    }

    // cout << left_entity[0][0] << endl;
    // cout << left_entity[0][1] << endl;
    cout << left_num[0] << endl;

    return 0;
}