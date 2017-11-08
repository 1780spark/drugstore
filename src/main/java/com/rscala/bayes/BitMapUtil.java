package com.rscala.bayes;

/**
 * bitMap是一种高效的数据结构，在Java中一个int类型数据占32位bit，在对大数据量场景对的int类型进行排序、去重将会耗费非常多内存，
 * 且性能低下，这种情况下bitMap将会发挥其高效作用，具体的原理以及关键实现如下：
 * <p>
 *  说明1：
 *     1个int类型占32位bit，采用bitMap数据结构主要是在内存开辟一个很大的int[]数据组,将int[]的每一个bit映射不同的整数数据；当然使用多少位bit进行映射是可以根据需要自定义。
 *     假设要处理的数据中最大值为numSize,且使用bNum位进行数据映射，则需要开辟的int[]内存刷空间大小length=numSize/(32/bNum)
 *     如用2位bit映射一个整数的话，要处理的整数数据集合中最大的值为1024，那么需要初始化最大的int[]为int[1024*2/32]即int[64]
 *   说明2：
 *     使用多位bit映射数据可以附带更多的说明信息，更加有效的进行排序、去重计算，如2bit，可以表示的数据情况有4种，即
 *      * --------------------
 * 		* 00 * 不存在        *
 * 		* 01 * 存在，仅有1次 *
 * 		* 10 * 存在，2次     *
 * 		* 11 * 存在，多次    *
 * 		*---------------------
 * 	  说明3：
 *      该数据结构实现的关键是bit位的映射，即int[]的下标确定：
 *   	1、待压入的数据为x,则int[]数据组的下标i为 i=x/16 即i=x >> 4   (位移计算的效率更高>>4 相当除以2的4次方)
 *   	2、数据填充在bit位置为j=x%16
 *
 *  下标     ---------高位----------bit--------------低位-------
 *   0   00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
 *   0   00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00  （<---第1次压如数字10的时候）
 *   0   00 00 00 00 00 00 10 00 00 00 00 00 00 00 00 00  （<---第2次压如数字10的时候）
 *   0   00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00  （<---第1次压如数字06的时候）
 *   0   00 00 00 00 00 00 11 00 00 00 00 00 00 00 00 00  （<---第3次压如数字10的时候）
 *   0   00 00 00 00 00 00 11 00 00 00 00 00 00 00 00 00  （<---第4次压如数字10的时候）
 *
 *      说明4：
 *     	   将数据映射好后，对bitMap按照末扫描到头，得到的数据映射还原就是倒序排序的，时间复杂度为O(n),非常高效
 *  </p>
 */
public class BitMapUtil {
	public BitMapUtil(int numSize){
		this.numSize=numSize;
		initBitMap();
	}

	/**
	 * 用两个bit为来标记某个元素的个数
	 */
	int bNum=2;
	/**
	 * 一个32位字节能标记多少个数
	 */
    int bSize=32/bNum;

	/**
	 * 能映射的最大值
	 */
	int numSize = 160000;
    

	/**
	 * 定义bitmap数组大小，开辟内存空间
	 */
    int arraySize =(int)Math.ceil((double)numSize/bSize);
    private int array[] = new int[arraySize];

    /**
     * 初始化BitMap
     */
    public void initBitMap(){
        for(int i=0;i<array.length;i++){
            array[i] = 0;
        }
    }

    /**
     * 往BitMap中设置对应的数的个数
     * @param x  要添加的数
     * @param num 对应的个数
     */
    public void set(int x,int num){
        //获得bitMap的下标
         int m = x /bSize;
        //获得对应的位置
        int n = x % bSize;
        //将x对应位置上的数值先清零，但是有要保证其他位置上的数不变
        array[m] &= ~((0x3<<(2*n)));
        //重新对x的个数赋值
        array[m] |= ((num&3)<<(2*n));
    }

    /**
     * 获取x在BitMap中的数量
     * @param x
     * @return
     */
    public int get(int x){
        int m = x >> 4;
        int n = x % bSize;
        //先去除该位置后面的bit置0，然后在移位取出该位填充的值，小于3就
        return (array[m] & (0x3<<(2*n))) >> (2*n);
    }

    /**
     * 往BitMap中添加数
     * 如果x的个数大于3不处理(2个bit定义11这种没有意义或表示已经重复多次了)
     * @param x
     */
    public void add(int x){
        int num = get(x);
        //只处理num小于3的
        if(num<3) {
            set(x, num + 1);
        }
    }


	/**
	 * 生成随机数组
	 * @param length
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
    public int[] getRandomInts(int length,int minValue,int maxValue){
		int[] randomInt=new int[length];
		for(int i=0;i<length;i++){
			randomInt[i]=getRandomInt(minValue,maxValue);
		}
		return randomInt;
	}

	/**
	 *
	 * @param minValue
	 * @param maxValue
	 * @return
	 */
	public int getRandomInt(int minValue,int maxValue){
		return (int)(Math.random()*(maxValue-minValue+1)+minValue);
	}

	/**
	 * 打印数组内容
	 * @param array
	 */
	public void printInt(int[] array){
		for(int i:array){
			System.out.print(i+",");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		int maxSize=20;
		BitMapUtil test = new BitMapUtil(maxSize);
		int sortArray[]=test.getRandomInts(maxSize,5,maxSize);
		test.printInt(sortArray);
		for(int i=0;i<sortArray.length;i++){
			test.add(sortArray[i]);
		}
		System.out.println("对BitMap中的所有去除重复数据排序:");
		for(int i=0;i<test.numSize;i++){
			if(test.get(i) !=0){
				System.out.print(i+" ");
			}
		}

		System.out.println("\n只出现1次的数据:");
		for(int i=0;i<test.numSize;i++){
			if(test.get(i) == 1){
				System.out.print(i+" ");
			}
		}

		System.out.println("\n只出现2次的数据:");
		for(int i=0;i<test.numSize;i++){
			if(test.get(i) == 2){
				System.out.print(i+" ");
			}
		}

		System.out.println("\n出现2次以上的数据:");
		for(int i=0;i<test.numSize;i++){
			if(test.get(i) > 2){
				System.out.print(i+" ");
			}
		}

	}
}