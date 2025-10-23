import { codegenNativeComponent, type ViewProps } from 'react-native';
import type {
  DirectEventHandler,
  Int32,
} from 'react-native/Libraries/Types/CodegenTypesNamespace';

export interface SelectionEvent {
  chosenOption: string;
  highlightedText: string;
  startIndex: Int32;
  endIndex: Int32;
}

interface NativeProps extends ViewProps {
  menuOptions: readonly string[];
  onSelection?: DirectEventHandler<SelectionEvent>;
}

export default codegenNativeComponent<NativeProps>('SelectableTextView');
