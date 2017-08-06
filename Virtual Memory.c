/**
 * Demonstration C program illustrating how to perform file I/O for vm assignment.
 *
 * Input file contains logical addresses
 * 
 * Backing Store represents the file being read from disk (the backing store.)
 *
 * We need to perform random input from the backing store using fseek() and fread()
 *
 * This program performs nothing of meaning, rather it is intended to illustrate the basics
 * of I/O for the vm assignment. Using this I/O, you will need to make the necessary adjustments
 * that implement the virtual memory manager.
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

// number of characters to read for each line from input file
#define BUFFER_SIZE         10

// number of bytes to read
#define CHUNK               99
//Andrew Goldman
FILE    *address_file;
FILE    *backing_store;

// how we store reads from input file
char    address[BUFFER_SIZE];

int     logical_address;

// the buffer containing reads from backing store
signed char     buffer[CHUNK];
int     pagetable[256];
signed char 	memory[256][256];


// the value of the byte (signed char) in memory
signed char     value;
//make 2d array
//use struct and make array of struct
//store page number and frame you're associate it
//assign frame to that page
//use char array for frames (0-256)
//oopen file, fseek to 
//tlb minature 

struct tlbstuff
{
	int a;
	int b;
};	

int main(int argc, char *argv[])
{
	int tlbhits = 0;
	int tlbcounter = 0;
	struct tlbstuff tlb[16];
	int z = 0;

	//initialize tlb
	for(z; z<16; z++)
	{
		tlb[z].a=-1;
		tlb[z].b=-1;
	}
	
	int counter = 0;
	//initialize pagetable
	for(counter;counter<256;counter++)
	{
		pagetable[counter] = -1;
	}

	counter = 0;
	int j = 0;
	for(counter;counter<256;counter++)
	{
		for(j;j<256;j++)
		{
			memory[counter][j] = -1;
		}	
	}
    // perform basic error checking
    if (argc != 3) {
        fprintf(stderr,"Usage: ./vm [backing store] [input file]\n");
        return -1;
    }

    // open the file containing the backing store
    backing_store = fopen(argv[1], "rb");
    
    if (backing_store == NULL) { 
        fprintf(stderr, "Error opening %s\n",argv[1]);
        return -1;
    }

    // open the file containing the logical addresses
    address_file = fopen(argv[2], "r");

    if (address_file == NULL) {
        fprintf(stderr, "Error opening %s\n",argv[2]);
        return -1;
    }

	
int i = 0;
    // read through the input file and output each logical address
    while ( fgets(address, BUFFER_SIZE, address_file) != NULL) {
        logical_address = atoi(address);
	
	int maskpage = 0xff00;
	int maskoffset = 0x00ff;

	//bit mask to find page and offset
	int page = logical_address&maskpage;
	int offset = logical_address&maskoffset;
	//shift to finish
	page = page >> 8;
	
	int frame = -1;
	z=0;
	for(z; z<16; z++)
	{
		if(tlb[z].a==page)
		{
			frame = tlb[z].b;
		}
	}
	
	

	if(frame==-1)//if we don't find a frame. we need to read it from the backing store
	{
		frame = pagetable[page];
		if(frame == -1)
		{
			// first seek to byte CHUNK in the backing store
        		// SEEK_SET in fseek() seeks from the beginning of the file
        		if (fseek(backing_store, 256*page, SEEK_SET) != 0) 
			{
            			fprintf(stderr, "Error seeking in backing store\n");
            			return -1;
       			}

        		// now read CHUNK bytes from the backing store to the buffer
        		if (fread(memory[i], sizeof(signed char), 256, backing_store) == 0)
			{//because there's 256 addresses
			
            			fprintf(stderr, "Error reading from backing store\n");
            			return -1;
        		}
		
		
		pagetable[page]=i;
		frame = i;		
		i++;
		}
		tlb[tlbcounter].a=page;
		tlb[tlbcounter].b=frame;
		tlbcounter = (tlbcounter+1)%16;
		
	} 
	else
	{//if we do find it, we need to increase tlb hits
		tlbhits++;
	}
	int framemirror = (frame<<8)|offset;
	
	printf("Virtual address: %d Physical address: %d Value: %d\n", logical_address, framemirror, memory[frame][offset]);
	printf("%d\n",tlbhits);

        
       
        // arbitrarily retrieve the 50th byte from the buffer 
    }
	fclose(address_file);
    	fclose(backing_store);

    return 0;
}




