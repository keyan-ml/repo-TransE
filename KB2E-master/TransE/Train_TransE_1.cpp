#include<iostream>
#include<fstream>
#include<cstring>
#include<cstdio>
#include<map>
#include<vector>
#include<string>
#include<ctime>
#include<cmath>
#include<cstdlib>
using namespace std;


#define pi 3.1415926535897932384626433832795
#define NBATCHS 50
#define NEPOCH 500

bool L1_flag = 1;

//normal distribution
double rand(double min, double max)   //产生一个min和max之间的随机数
{
    return min + (max - min) * rand() / (RAND_MAX + 1.0);
}
double normal(double x, double miu, double sigma)   //返回x的概率密度 
{
    return 1.0 / sqrt(2 * pi) / sigma * exp(-1 * (x - miu) * (x - miu) / (2 * sigma * sigma));
}
double randn(double miu, double sigma, double min ,double max)	//返回一个大于或等于均值miu的概率密度并且属于[min,max]的数
{
    double x, y, dScope;
    do {
        x = rand(min, max);
        y = normal(x, miu, sigma);
        dScope = rand(0.0, normal(miu, miu, sigma));
    } while(dScope > y);
    return x;
}

double sqr(double x)
{
    return x*x;
}

double vec_len(vector<double> &a)//返回向量a的模 
{
	double res = 0;
    for (int i = 0; i < a.size(); i++)
		res += a[i] * a[i];
	res = sqrt(res);
    return res;
}

// my function **********************************
// vector<string> mSplit(string str, string split_string) {
//     int cnt = 1;
//     char split_char = split_string[0];
//     for(int i = 0; i < str.length(); i++) {
//         if(str[i] == split_char) {
//             cnt++;
//         }
//     }

//     vector<string> strs(cnt);
//     for(int i = 0, offset = 0; i < cnt && offset < str.length(); i++, offset++) {
//         strs[i] = "";
//         for(int j = offset; str[j] != split_char && offset < str.length(); j++, offset++) {
//             strs[i] += str[j];
//         }
//     }
//     return strs;
// }

// void loadVectors(string filePath, map<string, int> &mentity2id,  map<int, string> &mid2entity, vector<vector<double>> &entity2vec) {
//     ifstream fr;
//     fr.open(filePath);

//     string str;
//     getline(fr, str);
//     int id = 0;
//     while(!fr.eof()) {
//         vector<string> parts = mSplit(str, " ");
//         string entity = parts[0];

//         entity2vec.resize(id + 1);
//         entity2vec[id].resize(parts.size() - 1);

//         mentity2id[entity] = id;
//         mid2entity[id] = entity;
//         for(int i = 1; i < parts.size(); i++) {
//             entity2vec[id][i - 1] = atof(parts[i].c_str());
//         }

//         cout << "load vector #" << id++ << endl;
//         getline(fr, str);
//     }
//     fr.close();
// }
// my function **********************************


string version;
char buf[100000], buf1[100000];
int relation_num, entity_num;
map<string,int> relation2id, entity2id;
map<int,string> id2entity, id2relation;


map<int,map<int,int> > left_entity,right_entity;
map<int,double> left_num,right_num;

class Train{
public:
	map<pair<int,int>, map<int,int>> ok;
    //通过头、尾、关系的id分别添加到相应的数组中，并标记这个元组
    void add(int x, int y, int z) { 
        // x, y, z: 左实体id, 右实体id, 关系id
        fb_h.push_back(x);
        fb_r.push_back(z);
        fb_l.push_back(y);
        ok[make_pair(x, z)][y] = 1;
    }
    void run(int n_in, double rate_in, double margin_in, int method_in) {
        // string filePath = "../../../Util/word_library/transe/yuliao_vector.txt";
        // map<string, int> mentity2id;
        // map<int, string> mid2entity;
        // vector<vector<double>> mentity2vec;
        // loadVectors(filePath, mentity2id, mid2entity, mentity2vec);

        n = n_in;	//嵌入维数
        rate = rate_in;
        margin = margin_in;
        method = method_in;
		
         /*分别设置关系向量和实体向量的数目和维数*/ 
        relation_vec.resize(relation_num);
		for (int i = 0; i < relation_vec.size(); i++)
			relation_vec[i].resize(n);
        entity_vec.resize(entity_num);
		for (int i = 0; i < entity_vec.size(); i++)
			entity_vec[i].resize(n);
        relation_tmp.resize(relation_num);
		for (int i = 0; i < relation_tmp.size(); i++)
			relation_tmp[i].resize(n);
        entity_tmp.resize(entity_num);
		for (int i = 0; i < entity_tmp.size(); i++)
			entity_tmp[i].resize(n);
		
		/*分别设置关系向量和实体向量的数目和维数*/
        //论文中提到的对关系向量进行归一化处理
        for (int i = 0; i < relation_num; i++) {
            for (int ii = 0; ii < n; ii++)
                relation_vec[i][ii] = randn(0, 1.0 / n, -6 / sqrt(n), 6 / sqrt(n));
        }
        //对实体向量进行归一化处理
        for (int i = 0; i < entity_num; i++) {
            // string entity = id2entity[i];
            // int id = mentity2id[entity];
            // entity_vec[i] = mentity2vec[id];
            
            //随机生成向量
            for (int ii = 0; ii < n; ii++)
                entity_vec[i][ii] = randn(0, 1.0 / n, -6 / sqrt(n), 6 / sqrt(n));
            
            //限制每个实体向量的模在1以内(单位化)
            norm(entity_vec[i]);
        }

        bfgs();
    }

private:
    int n, method;
    double res;//loss function value
    double count, count1;//loss function gradient
    double rate, margin;
    double belta;
    vector<int> fb_h, fb_l, fb_r;
    vector<vector<int>> feature;
    vector<vector<double>> relation_vec, entity_vec;
    vector<vector<double>> relation_tmp, entity_tmp;

    //限制实体向量a的模在1以内 
    double norm(vector<double> &a) {
        double x = vec_len(a);
        if (x > 1)
        for (int ii = 0; ii < a.size(); ii++)
                a[ii] /= x;
        return 0;
    }

    //返回一个属于区间[0,x)的整数 
    int rand_max(int x) {
        int res = (rand() * rand()) % x;
        while (res < 0)
            res += x;
        return res;
    }

    void bfgs() {
        res = 0;
        int nbatches = NBATCHS;
        int nepoch = NEPOCH;	//最大迭代次数 
        int batchsize = fb_h.size() / nbatches;
        for (int epoch = 0; epoch < nepoch; epoch++) {
            res = 0;
            for (int batch = 0; batch < nbatches; batch++)
            {
                relation_tmp = relation_vec;
                entity_tmp = entity_vec;
                for (int k = 0; k<batchsize; k++)
                {
                    int i = rand_max(fb_h.size());	//从头实体数组中产生一个下标  
                    int j = rand_max(entity_num);	//从总实体数组中产生一个下标 
                    double pr = 1000 * right_num[fb_r[i]] / (right_num[fb_r[i]] + left_num[fb_r[i]]); // 计算是替换头实体还是尾实体的概率  
                    if (method == 0)
                        pr = 500;
                    if (rand() % 1000 < pr)
                    { // 替换尾实体
                        while (ok[make_pair(fb_h[i], fb_r[i])].count(j) > 0)
                            j = rand_max(entity_num);
                        train_kb(fb_h[i], fb_l[i], fb_r[i], fb_h[i], j, fb_r[i]);//训练正确元组和替换了尾实体的元组  
                        
                        // cout << "fb_h[i]: " << fb_h[i] << endl;
                        // train_kb(fb_h[i], fb_l[i], fb_r[i], fb_h[i], fb_l[j], fb_r[i]);//训练正确元组和替换了尾实体的元组
                    }
                    else
                    { // 替换头实体
                        while (ok[make_pair(j,fb_r[i])].count(fb_l[i])>0)
                            j=rand_max(entity_num);
                        train_kb(fb_h[i], fb_l[i], fb_r[i], j, fb_l[i], fb_r[i]);//训练正确元组和替换了头实体的元组
                        // train_kb(fb_h[i], fb_l[i], fb_r[i], fb_h[j], fb_l[i], fb_r[i]);//训练正确元组和替换了头实体的元组
                    }
                        /*限制调整后的实体关系的模小于1*/
                    norm(relation_tmp[fb_r[i]]);
                    norm(entity_tmp[fb_h[i]]);
                    norm(entity_tmp[fb_l[i]]);
                    norm(entity_tmp[j]);
                        /*限制调整后的实体关系的模小于1*/ 
                }
                
                relation_vec = relation_tmp;
                entity_vec = entity_tmp;
            }
            cout<<"epoch:"<<epoch<<' '<<res<<endl;
            //保存关系向量和实体向量的值 
            FILE* f2 = fopen(("relation2vec." + version).c_str(), "w");
            FILE* f3 = fopen(("entity2vec." + version).c_str(), "w");
            for (int i = 0; i < relation_num; i++)
            {
                fprintf(f2, "%s\t", id2relation[i]);
                for (int ii = 0; ii < n; ii++)
                    fprintf(f2, "%.6lf\t", relation_vec[i][ii]);
                fprintf(f2, "\n");
            }
            for (int i=0; i<entity_num; i++)
            {
                fprintf(f3, "%s\t", id2entity[i]);
                for (int ii=0; ii<n; ii++)
                    fprintf(f3,"%.6lf\t",entity_vec[i][ii]);
                fprintf(f3,"\n");
            }
            fclose(f2);
            fclose(f3);
        }
    }
    double res1;
    double calc_sum(int e1,int e2,int rel) //计算实体e2和e1+rel的距离
    {
        double sum = 0;
        if (L1_flag) {
        	for (int ii=0; ii<n; ii++)
            	sum += fabs( entity_vec[e2][ii] - entity_vec[e1][ii] - relation_vec[rel][ii] ); // L1距离
            // 	sum += pow( entity_vec[e2][ii] - entity_vec[e1][ii] - relation_vec[rel][ii] , 2);
            // sum = sqrt(sum);
        }
        else
        	for (int ii=0; ii<n; ii++)
            	sum+=sqr(entity_vec[e2][ii]-entity_vec[e1][ii]-relation_vec[rel][ii]);//L2距离
        return sum;
    }
    void gradient(int e1_a, int e2_a, int rel_a, int e1_b, int e2_b, int rel_b)
    {
        for (int ii = 0; ii < n; ii++)
        {

            double x = 2*(entity_vec[e2_a][ii] - entity_vec[e1_a][ii] - relation_vec[rel_a][ii]);
            
            // ## 注意：这里用到了 L1_flag，且影响到了 所有向量值 的调整，需要重新考虑 cal_sum() 的计算方法 ##
            if (L1_flag)
            	if (x>0)
            		x=1;
            	else
            		x=-1;
            /*缩短正确元组中的e2_a和e1_a+rel_a的距离*/  
            relation_tmp[rel_a][ii] -= -1 * rate * x;
            entity_tmp[e1_a][ii] -= -1 * rate * x;
            entity_tmp[e2_a][ii] += -1 * rate * x;
            /*缩短正确元组中的e2_a和e1_a+rel_a的距离*/  
            x = 2 * (entity_vec[e2_b][ii] - entity_vec[e1_b][ii] - relation_vec[rel_b][ii]);
            if (L1_flag)
            	if (x>0)
            		x=1;
            	else
            		x=-1;
            /*增加错误元组中的e2_b和e1_b+rel_b的距离*/  
            relation_tmp[rel_b][ii] -= rate * x;
            entity_tmp[e1_b][ii] -= rate * x;
            entity_tmp[e2_b][ii] += rate * x;
            /*增加错误元组中的e2_b和e1_b+rel_b的距离*/  
        }
    }
    void train_kb(int e1_a,int e2_a,int rel_a,int e1_b,int e2_b,int rel_b)
    {
        double sum1 = calc_sum(e1_a, e2_a, rel_a);
        double sum2 = calc_sum(e1_b, e2_b, rel_b);
        if (sum1 + margin > sum2)
        {
        	res += margin + sum1 - sum2;
        	gradient( e1_a, e2_a, rel_a, e1_b, e2_b, rel_b);
        }
    }
};

Train train;

void prepare()
{
    FILE* f1 = fopen("../data/WN18/entity2id.txt", "r");
	FILE* f2 = fopen("../data/WN18/relation2id.txt", "r");
	int x;

    //读入实体 
	while (fscanf(f1, "%s%d", buf, &x) == 2) {
		string st = buf;
		entity2id[st] = x;
		id2entity[x] = st;
		entity_num++;
	}
    fclose(f1);
    //读入实体 

    //读入关系  
	while (fscanf(f2, "%s%d", buf, &x) == 2) {
		string st = buf;
		relation2id[st] = x;
		id2relation[x] = st;
		relation_num++;
	}
    fclose(f2);
    //读入关系 

    FILE* f_kb = fopen("../data/WN18/train.txt", "r");
    //读入训练集 
	while (fscanf(f_kb, "%s", buf) == 1) {
        string s1 = buf;
        fscanf(f_kb, "%s", buf);
        string s2 = buf;
        fscanf(f_kb, "%s", buf);
        string s3 = buf;

        if (entity2id.count(s1) == 0) {
            cout << "miss entity:" << s1 << endl;
        }
        if (entity2id.count(s2) == 0) {
            cout << "miss entity:" << s2 << endl;
        }
        if (relation2id.count(s3) == 0) {
            relation2id[s3] = relation_num;
            relation_num++;
        }

        // 同时有 s3关系，s1左实体的三元组的个数加1  
        left_entity[relation2id[s3]][entity2id[s1]]++;
        // 同时有 s3关系，s2右实体的三元组的个数加1
        right_entity[relation2id[s3]][entity2id[s2]]++;
        
        // test
        // cout << "left_entity[" << relation2id[s3] << "][" << entity2id[s1] << "] = " << left_entity[relation2id[s3]][entity2id[s1]] << endl;
        // test
        //添加元组（左实体id，右实体id，关系id）
        train.add(entity2id[s1], entity2id[s2], relation2id[s3]);
    } // while
    fclose(f_kb);

    cout << "relation_num=" << relation_num << endl;
    cout << "entity_num=" << entity_num << endl;

    for (int i = 0; i < relation_num; i++) {
    	double sum1 = 0, sum2 = 0;
    	for (map<int,int>::iterator it = left_entity[i].begin(); it != left_entity[i].end(); it++) {
    		sum1++; // 在 关系i 下，左实体的个数
    		sum2 += it -> second; // 在 关系i 下，所有元组的个数
    	}
    	left_num[i] = sum2 / sum1; //计算在关系i下，训练集中左实体的平均id

    }
    for (int i = 0; i < relation_num; i++) {
    	double sum1 = 0, sum2 = 0;
    	for (map<int,int>::iterator it = right_entity[i].begin(); it!=right_entity[i].end(); it++) {
    		sum1++; // 在 关系i 下，右实体的个数
    		sum2 += it -> second; // 在 关系i 下，所有元组的个数
    	}
    	right_num[i] = sum2 / sum1; //计算在关系i下，训练集中右实体的平均id  
    }
}

int ArgPos(char *str, int argc, char **argv) {
    int a;
    for (a = 1; a < argc; a++) 
        if (!strcmp(str, argv[a])) {
            if (a == argc - 1) {
                printf("Argument missing for %s\n", str);
                exit(1);
            }
            return a;
        }
    return -1;
}

int main(int argc, char**argv)
{
    srand((unsigned) time(NULL));
    int method = 1;
    int n = 20;
    double rate = 0.001;
    double margin = 5;
    
    // 判断是否为 带参数执行
    int i;
    if ((i = ArgPos((char *)"-size", argc, argv)) > 0)
        n = atoi(argv[i + 1]);
    if ((i = ArgPos((char *)"-margin", argc, argv)) > 0) 
        margin = atoi(argv[i + 1]);
    if ((i = ArgPos((char *)"-method", argc, argv)) > 0) 
        method = atoi(argv[i + 1]);
    // 判断是否为 带参数执行

    cout << "size = " << n << endl;
    cout << "learing rate = " << rate << endl;
    cout << "margin = " << margin << endl;

    if (method)
        version = "bern";
    else
        version = "unif";
    cout << "method = " << version << endl;

    prepare();
    train.run(n, rate, margin, method);
    
    return 0;
}


