package battlecode.server.proxy;

import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;
import battlecode.serial.ExtensibleMetadata;
import battlecode.serial.MatchHeader;
import battlecode.serial.MatchFooter;
import battlecode.serial.RoundDelta;
import battlecode.serial.RoundStats;
import battlecode.world.InternalTerrainTile;
import battlecode.world.GameMap;
import battlecode.engine.signal.Signal;
import battlecode.engine.signal.SignalHandler;
import java.io.OutputStream;
import java.lang.NumberFormatException;
import java.lang.reflect.Method;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.StreamCorruptedException;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class XStreamProxy extends Proxy {

	OutputStream stream;
	static XStream xstream;

	public static class IntArrayConverter implements SingleValueConverter {

		public boolean canConvert(Class cls) {
			return cls.equals(int [].class);
		}

		public String toString(Object obj) {
			return StringUtils.join(ArrayUtils.toObject((int [])obj), ",");
		}

		public Object fromString(String name) {
			String [] strings = name.split(",");
			int [] obj = new int [ strings.length ];
			int i;
			try {
				for(i=0;i<strings.length;i++)
					obj[i] = Integer.parseInt(strings[i]); 
			} catch(NumberFormatException e) {
				throw new ConversionException("Invalid int []",e);
			}
			return obj;
		}
	}

	public static class LongArrayConverter implements SingleValueConverter {
		
		public boolean canConvert(Class cls) {
			return cls.equals(long [].class);
		}

		public String toString(Object obj) {
			return StringUtils.join(ArrayUtils.toObject((long [])obj), ",");
		}

		public Object fromString(String name) {
			String [] strings = name.split(",");
			long [] obj = new long [ strings.length ];
			int i;
			try {
				for(i=0;i<strings.length;i++)
					obj[i] = Long.parseLong(strings[i]); 
			} catch(NumberFormatException e) {
				throw new ConversionException("Invalid long []",e);
			}
			return obj;
		}

	}

	public static class DoubleArrayConverter implements SingleValueConverter {

		public boolean canConvert(Class cls) {
			return cls.equals(double [].class);
		}

		public String toString(Object obj) {
			return StringUtils.join(ArrayUtils.toObject((double [])obj), ",");
		}

		public Object fromString(String name) {
			String [] strings = name.split(",");
			double [] obj = new double [ strings.length ];
			int i;
			try {
				for(i=0;i<strings.length;i++)
					obj[i] = Double.parseDouble(strings[i]); 
			} catch(NumberFormatException e) {
				throw new ConversionException("Invalid double []",e);
			}
			return obj;
		}
	}

	public static class MapLocationConverter implements SingleValueConverter {

		public boolean canConvert(Class cls) {
			return cls.equals(MapLocation.class);
		}
		
		public String toString(Object obj) {
			MapLocation loc = (MapLocation)obj;
			return String.format("%s,%s",loc.getX(),loc.getY());
		}

		public Object fromString(String name) {
			String [] coords = name.split(",");
			if(coords.length!=2)
				throw new ConversionException("Invalid MapLocation");
			try {
				return new MapLocation(Integer.parseInt(coords[0]),Integer.parseInt(coords[1]));
			} catch(NumberFormatException e) {
				throw new ConversionException("Invalid MapLocation",e);
			}
		}

	}

	public static class RoundDeltaConverter implements Converter {

		public boolean canConvert(Class cls) {
			return cls.equals(RoundDelta.class);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
							MarshallingContext context) {
			context.convertAnother(((RoundDelta)value).getSignals());
        }

        public Object unmarshal(HierarchicalStreamReader reader,
                        UnmarshallingContext context) {
			RoundDelta rd = new RoundDelta();
			rd.setSignals((Signal [])context.convertAnother(rd,Signal [].class));
			return rd;
        }

	}

	public static class ExtensibleMetadataConverter implements Converter {

		public boolean canConvert(Class cls) {
			return cls.equals(ExtensibleMetadata.class);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
							MarshallingContext context) {
			ExtensibleMetadata metadata = (ExtensibleMetadata)value;
			for(String s : metadata.keySet()) {
				Object o = metadata.get(s,null);
				if(s.equals("maps"))
					writer.addAttribute(s,StringUtils.join((String [])o,","));
				else
					writer.addAttribute(s,o.toString());
			}
        }

        public Object unmarshal(HierarchicalStreamReader reader,
								UnmarshallingContext context) {
			ExtensibleMetadata metadata = new ExtensibleMetadata();
			int i;
			String name, value;
			for(i=0;i<reader.getAttributeCount();i++) {
				name = reader.getAttributeName(i);
				value = reader.getAttribute(i);
				if(name.equals("maps"))
					metadata.put(name,value.split(","));
				else
					metadata.put(name,value);
			}
			return metadata;
        }

	}

	public static class EmptyConverter implements Converter {

		public boolean canConvert(Class cls) {
			return true;
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
							MarshallingContext context) {
        }

        public Object unmarshal(HierarchicalStreamReader reader,
								UnmarshallingContext context) {

			return null;
        }
	}

	public static class MapTileConverter implements Converter {

		public static enum DataType { terrain, height }

		public boolean canConvert(Class cls) {
			return cls.equals(InternalTerrainTile [][].class);
		}

		public void writeTerrainData(InternalTerrainTile [][] tiles, HierarchicalStreamWriter writer, DataType type) {
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for(InternalTerrainTile [] row: tiles) {
				if(first)
					first=false;
				else
					builder.append("/");
				for(InternalTerrainTile t: row) {
					switch(type) {
					case terrain:
						builder.append(t.getType()==TerrainTile.TerrainType.LAND?".":"#");
						break;
					case height:
						builder.append(Integer.toString(t.getHeight(),Character.MAX_RADIX));
					}
				}
			}
			writer.addAttribute(type.toString(),builder.toString());
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
			MarshallingContext context) {
			InternalTerrainTile [][] tiles = (InternalTerrainTile [][])value;
			// I tried to do this in the easiest way possible, rather than
			// using the map file format.  Feel free to change.  -dgulotta
			writeTerrainData(tiles,writer,DataType.terrain);
			writeTerrainData(tiles,writer,DataType.height);
		}

		public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) throws ConversionException {
			String [] terrain = reader.getAttribute("terrain").split("/");
			String [] height = reader.getAttribute("height").split("/");
			if(terrain.length!=height.length) throw new ConversionException("incorrect terrain/heightmap size");
			InternalTerrainTile [][] tiles = new InternalTerrainTile [terrain.length][];
			for(int i=0;i<terrain.length;i++) {
				if(terrain[i].length()!=height[i].length()) throw new ConversionException("incorrect terrain/heightmap size");
				tiles[i] = new InternalTerrainTile [terrain[i].length()];
				for(int j=0;j<terrain[i].length();j++) {
					
					int h;
					TerrainTile.TerrainType t;
					try {
						h=Integer.parseInt(height[i].substring(j,j+1),Character.MAX_RADIX);
					} catch(NumberFormatException e) {
						throw new ConversionException("Illegal value in heightmap");
					}
					switch(terrain[i].charAt(j)) {
						case '#':
							t = TerrainTile.TerrainType.LAND;
							break;
						case '.':
						case ' ':
							t = TerrainTile.TerrainType.VOID;
							break;
						default:
							throw new ConversionException("Illegal value in terrain map");
					}
					tiles[i][j] = new InternalTerrainTile(h,t);
				}
			}
			return tiles;
		}

	}

	/*
	public static class MatchHeaderConverter implements Converter {

		public boolean canConvert(Class cls) {
			return cls.equals(MatchHeader.class);
		}

		public void marshal(Object value, HierarchicalStreamWriter writer,
							MarshallingContext context) {
        }

        public Object unmarshal(HierarchicalStreamReader reader,
                        UnmarshallingContext context) {
			return null;
        }

	}
	*/

	static protected void initXStream() {
		if(xstream!=null) return;
		xstream = new XStream() {
				public void reset() {}
			};
		xstream.registerConverter(new IntArrayConverter());
		xstream.registerConverter(new LongArrayConverter());
		xstream.registerConverter(new DoubleArrayConverter());
		xstream.registerConverter(new MapLocationConverter());
		xstream.registerConverter(new ExtensibleMetadataConverter());
		xstream.registerConverter(new MapTileConverter());
		//xstream.registerConverter(new MatchHeaderConverter());
		EmptyConverter empty = new EmptyConverter();
		xstream.registerLocalConverter(GameMap.class,"blockMap",empty);
		xstream.registerLocalConverter(GameMap.class,"initialBlockMap",empty);
		//xstream.registerLocalConverter(GameMap.class,"mapTiles",empty);
		xstream.registerConverter(new RoundDeltaConverter());
		xstream.useAttributeFor(int.class);
		xstream.useAttributeFor(int [].class);
		xstream.useAttributeFor(long.class);
		xstream.useAttributeFor(long [].class);
		xstream.useAttributeFor(double.class);
		xstream.useAttributeFor(double [].class);
		xstream.useAttributeFor(boolean.class);
		xstream.useAttributeFor(String.class);
		xstream.useAttributeFor(battlecode.common.Direction.class);
		xstream.useAttributeFor(battlecode.common.MapLocation.class);
		xstream.useAttributeFor(battlecode.common.RobotLevel.class);
		xstream.useAttributeFor(battlecode.common.RobotType.class);
		xstream.useAttributeFor(battlecode.common.Team.class);
		xstream.useAttributeFor(battlecode.serial.DominationFactor.class);
		//xstream.aliasPackage("","battlecode.world.signal");
	}

	static public XStream getXStream() {
		initXStream();
		return xstream;
	}

	public XStreamProxy(OutputStream stream) {
		this.stream = stream;
	}

	public void writeObject(Object o) throws IOException {
		// XStream object output streams do not support reset
		output.writeObject(o);
	}

	protected OutputStream getOutputStream() throws IOException {
		return getXStream().createObjectOutputStream(stream);
	}

	// In "compute and view match synchronously" mode, we can get
	// stuck if we don't flush every round
	public void writeStats(RoundStats stats) throws IOException {
		writeObject(stats);
		output.flush();
	}

	public void writeFooter(MatchFooter footer) throws IOException {
		writeObject(footer);
		output.flush();
	}

	static private final int EX_USAGE = 64;
	static private final int EX_DATAERR = 65;
	static private final int EX_IOERR = 74;

	public static void usage() {
		System.err.println("Usage: XStreamProxy [-z] [file]");
		System.exit(EX_USAGE);
	}

	public static void main(String [] args) {
		Options options = new Options();
		options.addOption("z","gzip",false,"open a gzip compressed file");
		CommandLine cl = null;
		try {
			cl = new GnuParser().parse(options,args);
		} catch(ParseException e) {
			usage();
			return;
		}
		XStreamProxy proxy = null;
		try {
			InputStream stream;
			switch(cl.getArgs().length) {
			case 0:
				stream = System.in;
				break;
			case 1:
				stream = new FileInputStream(cl.getArgs()[0]);
				break;
			default:
				usage();
				return;
			}
			if(cl.hasOption('z'))
				stream = new GZIPInputStream(stream);
			proxy = new XStreamProxy(System.out);
			System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			proxy.open();
			ObjectInputStream input = new ObjectInputStream(stream);
			while(true) {
				proxy.writeObject(input.readObject());
			}
		} catch(EOFException e) {
			try {
				proxy.close();
			} catch(IOException e2) {
				e2.printStackTrace();
				System.exit(EX_IOERR);
			}
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(EX_DATAERR);			
		} catch(IOException e) {
			e.printStackTrace();
			if(e instanceof StreamCorruptedException ||
			   e.getMessage().equals("Not in GZIP format"))
				System.exit(EX_DATAERR);
			else
				System.exit(EX_IOERR);
		}
	}

}
