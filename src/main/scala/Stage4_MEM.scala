import chisel3._
import chisel3.util._
import lib.ControlBus

class Stage4_MEM(fpga: Boolean, mem_size: Int) extends Module {
  val io = IO(new Bundle{
    val data_write = Input(SInt(32.W))
    val data_in = Input(SInt(32.W))
    val rd_in = Input(UInt(5.W))
    val ctrl_in = Input(new ControlBus)
    val data_out_mem = Output(SInt(32.W))
    val data_out_alu = Output(SInt(32.W))
    val rd_out = Output(UInt(5.W))
    val ctrl_out = Output(new ControlBus)
  })

  // Data memory
  val data_memory = Seq.fill(4)(Module(new MemoryData(fpga, mem_size)))

  // Calculate data to write
  val STORE_BYTE = 1.U
  val STORE_HALFWORD = 2.U
  val STORE_WORD = 3.U
  val LOAD_BYTE = 1.U
  val LOAD_HALFWORD = 2.U
  val LOAD_WORD = 3.U

  val address = io.data_in.asUInt & "xFFFFFFFC".U
  val byte_offset = io.data_in(1, 0)
  val write_data = VecInit(io.data_write(7, 0).asSInt, io.data_write(15, 8).asSInt,
                           io.data_write(23, 16).asSInt, io.data_write(31, 24).asSInt)
  val write_enable = VecInit(Seq.fill(4)(WireDefault(false.B)))

  val load_byte = WireDefault(0.S(8.W))
  val load_halfword = WireDefault(0.S(16.W))
  val load_word = WireDefault(0.S(32.W))
  val load_data = WireDefault(0.S(32.W))

  val store_type = io.ctrl_in.store_type


  // Initialize memory defaults
  for (mem <- data_memory) {
    mem.io.address := 0.U
    mem.io.data_in := 0.S
    mem.io.write_enable := false.B
  }

  // Assign addresses to data_memory
  (0 until 4).foreach { i =>
    when(byte_offset === i.U) {
      for (j <- 0 until 4) {
        data_memory(j).io.address := address + Mux((j.S - i.S) < 0.S, 4.U, 0.U)
      }
    }
  }

  // Store Byte
  (0 until 4).foreach { i =>
    when(store_type === STORE_BYTE && byte_offset === i.U) {
      data_memory(i).io.data_in := write_data(0)
      data_memory(i).io.write_enable := true.B
    }
  }

  // Store Half-word
  when(store_type === STORE_HALFWORD) {
    (0 until 4).foreach { i =>
      when(byte_offset === i.U) {
        data_memory(i).io.data_in := write_data(0)
        data_memory((i + 1) % 4).io.data_in := write_data(1)
        data_memory(i).io.write_enable := true.B
        data_memory((i + 1) % 4).io.write_enable := true.B
      }
    }
  }

  // Store Word
  when(store_type === STORE_WORD) {
    (0 until 4).foreach { i =>
      when(byte_offset === i.U) {
        for (j <- 0 until 4) {
          data_memory((i + j) % 4).io.data_in := write_data(j)
          data_memory((i + j) % 4).io.write_enable := true.B
        }
      }
    }
  }

  // Load Init
  val sign_extension : Bool = 1.B //io.ctrl_in.load_type(2) TEMP!!! UNSIGNED LOGIC ShOULD BE MADE
  val load_type = RegNext(io.ctrl_in.load_type(1,0))
  val byte_offset_load = RegNext(byte_offset)
  load_data := 0.S

  // Load Byte
  when(load_type === LOAD_BYTE) {
    (0 until 4).foreach { i =>
      when(byte_offset_load === i.U) {
        load_byte := data_memory(i).io.data_out
      }
    }
    load_data := Mux(sign_extension, load_byte.asSInt << 24 >> 24, load_byte.asSInt)
  }

  // Load Half-word
  when(load_type === LOAD_HALFWORD) {
    (0 until 4).foreach { i =>
      when(byte_offset_load === i.U) {
        load_halfword := Cat(data_memory((i + 1) % 4).io.data_out, data_memory(i).io.data_out).asSInt
      }
    }
    load_data := Mux(sign_extension, load_halfword.asSInt << 16 >> 16, load_halfword.asSInt)
  }

  // Load Word
  when(load_type === LOAD_WORD) {
    (0 until 4).foreach { i =>
      when(byte_offset_load === i.U) {
        load_word := Cat(data_memory((i + 3) % 4).io.data_out, data_memory((i + 2) % 4).io.data_out,
                         data_memory((i + 1) % 4).io.data_out, data_memory(i).io.data_out).asSInt
      }
    }
    load_data := load_word.asSInt
  }

  // Output
  io.data_out_mem := load_data
  io.data_out_alu := RegNext(io.data_in)
  io.rd_out := RegNext(io.rd_in)
  io.ctrl_out := RegNext(io.ctrl_in)
}