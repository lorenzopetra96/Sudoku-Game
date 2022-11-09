package com.github.lorenzopetra96.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Sudoku implements Serializable{
	
	int [][][] sudokuList = {
			
			{
				{8, 4, 3, 5, 7, 1, 6, 9, 2},
				{9, 1, 7, 8, 6, 2, 5, 4, 3},
				{6, 5, 2, 9, 4, 3, 1, 8, 7},
				{4, 3, 6, 1, 2, 5, 8, 7, 9},
				{2, 8, 5, 7, 3, 9, 4, 1, 6},
				{7, 9, 1, 4, 8, 6, 3, 2, 5},
				{5, 7, 9, 3, 1, 8, 2, 6, 4},
				{1, 6, 4, 2, 5, 7, 9, 3, 8},
				{3, 2, 8, 6, 9, 4, 7, 5, 1}
			},
			{
				{4, 3, 7, 9, 1, 2, 6, 5, 8},
				{8, 1, 6, 7, 5, 3, 2, 4, 9},
				{9, 2, 5, 8, 6, 4, 1, 7, 3},
				{2, 9, 3, 6, 7, 5, 4, 8, 1},
				{1, 6, 8, 2, 4, 9, 7, 3, 5},
				{5, 7, 4, 3, 8, 1, 9, 2, 6},
				{7, 8, 1, 4, 3, 6, 5, 9, 2},
				{6, 4, 2, 5, 9, 8, 3, 1, 7},
				{3, 5, 9, 1, 2, 7, 8, 6, 4}
			},
			{
				{8,6, 2, 7, 9, 4, 5, 3, 1},
				{5,3, 4, 8, 1, 6, 7, 9, 2},
				{1,9, 7, 3, 5, 2, 8, 6, 4},
				{9,8, 1, 2, 7, 3, 4, 5, 6},
				{4,2, 3, 6, 8, 5, 9, 1, 7},
				{6,7, 5, 9, 4, 1, 2, 8, 3},
				{2,4, 9, 1, 3, 8, 6, 7, 5},
				{3,5, 8, 4, 6, 7, 1, 2, 9},
				{7,1, 6, 5, 2, 9, 3, 4, 8}
			},
			{
				{5, 7, 2, 6, 1, 8, 3, 9, 4},
				{1, 9, 4, 5, 2, 3, 7, 8, 6},
				{8, 3, 6, 7, 9, 4, 1, 5, 2},
				{2, 8, 1, 3, 4, 5, 6, 7, 9},
				{9, 6, 7, 2, 8, 1, 4, 3, 5},
				{4, 5, 3, 9, 6, 7, 8, 2, 1},
				{6, 1, 9, 8, 7, 2, 5, 4, 3},
				{7, 4, 5, 1, 3, 9, 2, 6, 8},
				{3, 2, 8, 4, 5, 6, 9, 1, 7}
			},
			{
				{3, 5, 8, 2, 7, 4, 1, 9, 6},
				{4, 6, 7, 9, 3, 1, 8, 2, 5},
				{1, 9, 2, 5, 6, 8, 3, 7, 4},
				{7, 1, 9, 3, 4, 6, 5, 8, 2},
				{2, 8, 4, 1, 5, 7, 9, 6, 3},
				{5, 3, 6, 8, 2, 9, 4, 1, 7},
				{9, 4, 5, 7, 8, 2, 6, 3, 1},
				{6, 2, 1, 4, 9, 3, 7, 5, 8},
				{8, 7, 3, 6, 1, 5, 2, 4, 9}
			},
			{
				{5, 1, 6, 3, 4, 2, 8, 9, 7},
				{9, 3, 8, 7, 5, 6, 1, 4, 2},
				{4, 2, 7, 9, 8, 1, 3, 5, 6},
				{6, 9, 3, 5, 7, 4, 2, 1, 8},
				{8, 4, 5, 2, 1, 3, 7, 6, 9},
				{1, 7, 2, 8, 6, 9, 4, 3, 5},
				{2, 8, 9, 1, 3, 5, 6, 7, 4},
				{7, 6, 1, 4, 9, 8, 5, 2, 3},
				{3, 5, 4, 6, 2, 7, 9, 8, 1}

			},
			{
				{7, 8, 9, 1, 4, 2, 3, 5, 6},
				{5, 2, 3, 8, 6, 7, 1, 4, 9},
				{1, 6, 4, 5, 3, 9, 2, 7, 8},
				{4, 9, 8, 3, 2, 5, 6, 1, 7},
				{3, 5, 1, 6, 7, 8, 9, 2, 4},
				{6, 7, 2, 9, 1, 4, 8, 3, 5},
				{2, 3, 5, 7, 8, 6, 4, 9, 1},
				{9, 4, 6, 2, 5, 1, 7, 8, 3},
				{8, 1, 7, 4, 9, 3, 5, 6, 2}
			},
			{
				{3, 9, 6, 2, 8, 4, 5, 1, 7},
				{1, 2, 8, 7, 3, 5, 4, 9, 6},
				{5, 4, 7, 9, 6, 1, 2, 3, 8},
				{4, 8, 2, 5, 7, 3, 1, 6, 9},
				{6, 1, 3, 8, 9, 2, 7, 4, 5},
				{7, 5, 9, 4, 1, 6, 8, 2, 3},
				{2, 6, 5, 3, 4, 7, 9, 8, 1},
				{9, 7, 1, 6, 2, 8, 3, 5, 4},
				{8, 3, 4, 1, 5, 9, 6, 7, 2}
			},
			
	};
	
	
	private int random;
	
	private int[][] sudoku_risolto;
	
	private int[][] sudoku_sfida;
	
	public Sudoku(int random) {
		
		
		this.random = random;
		this.sudoku_risolto = sudokuList[random];
		this.sudoku_sfida = Arrays.stream(this.sudoku_risolto).map(int[]::clone).toArray(int[][]::new);
		complica_sudoku();
		
	}
	
	private void complica_sudoku(){
		
		
//		sudoku_sfida[3][3] = 0;
//		sudoku_sfida[4][4] = 0;
		
		for(int i=0;i<9;i++) {
			for(int j=0;j<9;j++) {
				if(i%2==0 && j%2==0) sudoku_sfida[i][j] = 0;
				else if( i%2==1 && j%2==1) sudoku_sfida[i][j] = 0;
			}
		}
		
	}

	public int[][] getSudoku_risolto() {
		return sudoku_risolto;
	}

	public void setSudoku_risolto(int[][] sudoku_risolto) {
		this.sudoku_risolto = sudoku_risolto;
	}

	public int[][] getSudoku_sfida() {
		return sudoku_sfida;
	}

	public void setSudoku_sfida(int[][] sudoku_sfida) {
		this.sudoku_sfida = sudoku_sfida;
	}
	
	public void printSudoku(int[][] sudoku) {
		
		String sudokuPrint = "";
		for(int i=0;i<9;i++) {
			for(int j=0;j<9;j++) {
				sudokuPrint += sudoku[i][j] + " ";
			}
			sudokuPrint += "\n";
		}
		
		System.out.println(sudokuPrint);
		
	}
	
	public int contaZeri(int[][] sudoku) {
		
		int count=0;
		for(int i=0;i<9;i++) {
			for(int j=0;j<9;j++) {
				if(sudoku[i][j]==0) count++;
			}
		}
		
		return count;
		
	}
	
	
	
	

}
