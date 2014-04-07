/* 
	lib_mysqludf_udf - a library for reporting internals from the udf interface.
	This is intended primarily for debugging purposes and discovery of undocumented behaviour
	
	Copyright (C) 2007  Roland Bouman 
	web: http://www.xcdsql.org/MySQL/UDF/ 
	email: mysqludfs@gmail.com
	
	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.
	
	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
#if defined(_WIN32) || defined(_WIN64) || defined(__WIN32__) || defined(WIN32)
#define DLLEXP __declspec(dllexport) 
#else
#define DLLEXP
#endif

#ifdef STANDARD
#include <string.h>
#include <stdlib.h>
#include <time.h>
#ifdef __WIN__
typedef unsigned __int64 ulonglong;
typedef __int64 longlong;
#else
typedef unsigned long long ulonglong;
typedef long long longlong;
#endif /*__WIN__*/
#else
#include <my_global.h>
#include <my_sys.h>
#endif
#include <mysql.h>
#include <m_ctype.h>
#include <m_string.h>
#include <stdlib.h>

#include <ctype.h>

#ifdef HAVE_DLOPEN
#ifdef	__cplusplus
extern "C" {
#endif

#define LIBVERSION "fuzzySearch version 0.1"

#ifdef	__cplusplus
}
#endif

FILE * debug = NULL;

double doSearch(char * term, int termLength, char * text, int textLength, int dist, int * scoreWindow)
{
	int cursor = 0;
	int windowLength = dist+1;
	int best = 0;
	int i, j, sum;

	//fprintf(debug, "doSearch(\"%.*s\", %d, \"%.*s\", %d, %d)\n", termLength, term, termLength, textLength, text, textLength, dist);

	for (i=0;i<windowLength;i++)
		scoreWindow[i] = 0;

	for (i = 1-termLength;i<textLength;i++)
	{
		scoreWindow[cursor] = 0;
		for (j=0;j<termLength;j++)
			if (i+j>=0 && i+j<textLength && term[j]==text[i+j])
				scoreWindow[cursor]++;
		
		if (scoreWindow[cursor] > best)
			best = scoreWindow[cursor];
		
		sum = scoreWindow[cursor];
		for (j=0;j<dist;j++)
		{
			sum += scoreWindow[(cursor+windowLength-(j+1))%windowLength];
			if (sum-(j+1) > best)
				best = sum-(j+1);
		}
		
		cursor = (cursor+1)%windowLength;
	}

	best = best > termLength ? termLength : best;
	return best*1./termLength;
}

DLLEXP
my_bool fuzzySearch_init(UDF_INIT * initid, UDF_ARGS * args, char * message)
{
	if (args->arg_count != 3)
	{
		strcpy(message, "Incorrect number of parameters!");
		return 1;
	}
	else if (args->arg_type[0] != STRING_RESULT || args->arg_type[1] != STRING_RESULT || args->arg_type[2] != INT_RESULT)
	{
		strcpy(message, "Incorrect parameter types!");
		return 1;
	}
	
	initid->ptr = malloc(sizeof(int)*((long)(*args->args[2])+1));
	//debug = fopen("C:\\sci\\mysql-5.5.9-win32\\lib\\plugin\\test.txt", "a");

	return 0;
}

DLLEXP
void fuzzySearch_deinit(UDF_INIT *initid)
{
	free(initid->ptr);
	//fclose(debug);
}

DLLEXP
char * fuzzySearch(UDF_INIT *initid, UDF_ARGS *args, char* result,	unsigned long *length, char *is_null, char *error)
{
	double dist = doSearch(
		(char *)(args->args[0]), args->lengths[0],
		(char *)(args->args[1]), args->lengths[1],
		(long)(*args->args[2]), (int *)(initid->ptr));
	sprintf(result, "%.2f", dist);
	*length = strlen(result);
	return result;
}

#endif /* HAVE_DLOPEN */
